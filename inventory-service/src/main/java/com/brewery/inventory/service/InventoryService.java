package com.brewery.inventory.service;

import com.brewery.common.exception.BusinessException;
import com.brewery.common.kafka.producer.ReactiveProducerService;
import com.brewery.inventory.mapper.InventoryMapper;
import com.brewery.inventory.repository.*;
import com.brewery.model.domain.InventoryDTO;
import com.brewery.model.dto.OrderDto;
import com.brewery.model.dto.OrderLineDto;
import com.brewery.model.dto.OrderStatus;
import com.brewery.model.event.BrewBeerEvent;
import com.brewery.model.event.OrderEvent;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brewery.common.exception.ExceptionReason.CUSTOMIZE_REASON;
import static com.brewery.common.exception.ExceptionReason.INTERNAL_SERVER_ERROR;
import static com.brewery.common.util.AppConstant.RESILIENCE_ID_MONGO;
import static com.brewery.common.util.AppConstant.TENANT_ID;
import static com.brewery.common.util.Helper.*;
import static com.brewery.inventory.util.ValidationResult.SUCCESS;
import static com.brewery.inventory.util.Validator.validateInventoryDTO;
import static com.brewery.model.dto.InventoryType.ALLOCATE;
import static com.brewery.model.dto.InventoryType.BREW;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryRepository inventoryRepository;

	private final InventoryLedgerRepository inventoryLedgerRepository;

	private final InventoryMapper inventoryMapper;

	private final TransactionalOperator transactionalOperator;

	private final ReactiveMongoOperations reactiveMongoOperations;

	private final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

	private final Retry mongoServiceRetry;

	private final ReactiveProducerService<String, OrderEvent> orderStatusProducer;

	public Mono<InventoryDTO> addInventory(BrewBeerEvent brewBeerEvent) {

		var validateHeaders = validateContext();

		var validateRequest = validateInventoryDTO(brewBeerEvent).flatMap(result -> SUCCESS.equals(result)
				? Mono.empty() : Mono.error(new BusinessException(CUSTOMIZE_REASON, result.name())));

		var persist = Mono.deferContextual(ctx -> {
			QInventory inventory = QInventory.inventory;
			return inventoryRepository
					.findOne(inventory.beerId.eq(brewBeerEvent.beerId())
							.and(inventory.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))))
					.transformDeferred(RetryOperator.of(mongoServiceRetry));
		}).map(inventory -> {
			inventory.setQtyOnHand(inventory.getQtyOnHand() + brewBeerEvent.qtyToBrew());
			return inventory;
		}).switchIfEmpty(Mono.just(inventoryMapper.fromBrewBeerEvent(brewBeerEvent)))
				.flatMap(__ -> inventoryRepository.save(__).transformDeferred(RetryOperator.of(mongoServiceRetry)))
				.flatMap(__ -> inventoryLedgerRepository.save(
						InventoryLedger.builder().inventoryId(__.getId()).type(BREW).referenceId(brewBeerEvent.id())
								.qty(brewBeerEvent.qtyToBrew()).totQty(__.getQtyOnHand()).build())
						.map(ledger -> __))
				.map(inventoryMapper::fromInventory).transform(it -> {
					ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create(RESILIENCE_ID_MONGO);
					return rcb.run(it, throwable -> {
						log.error("exception::", throwable);
						return Mono.error(new BusinessException(INTERNAL_SERVER_ERROR));
					});
				})
		// .as(transactionalOperator::transactional)
		// .transformDeferred(RetryOperator.of(mongoServiceRetry))
		;

		return validateHeaders.then(validateRequest).then(persist).onErrorResume(
				throwable -> Mono.error(new BusinessException(CUSTOMIZE_REASON, throwable.getMessage())));
	}

	public Flux<InventoryDTO> inventoryByBeerId(Collection<String> beerId) {

		var validate = validateContext();

		var beerInventory = Flux.deferContextual(ctx -> {
			QInventory inventory = QInventory.inventory;
			return inventoryRepository.findAll(inventory.beerId.in(beerId)
					.and(inventory.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))));
		}).map(inventoryMapper::fromInventory).transform(it -> {
			var rcb = reactiveCircuitBreakerFactory.create(RESILIENCE_ID_MONGO);
			return rcb.run(it, throwable -> {
				log.error("exception::", throwable);
				return Flux.error(new BusinessException(INTERNAL_SERVER_ERROR));
			});
		}).transformDeferred(RetryOperator.of(mongoServiceRetry));

		return validate.thenMany(beerInventory);

	}

	public Mono<?> allocateInventory(OrderEvent value) {
		var validateHeaders = validateContext();

		var persist = Flux.deferContextual(ctx -> {
			var beerId = collectionAsStream(value.orderDto().orderLine()).map(OrderLineDto::beerId)
					.collect(Collectors.toList());
			QInventory inventory = QInventory.inventory;
			return inventoryRepository
					.findAll(inventory.beerId.in(beerId)
							.and(inventory.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))))
					.transformDeferred(RetryOperator.of(mongoServiceRetry));
		}).collectList().flatMap(__ -> {
			var inventoryList = new ArrayList<Inventory>();
			var inventoryLedgerList = new ArrayList<InventoryLedger>();
			var map = collectionAsStream(value.orderDto().orderLine())
					.collect(Collectors.toMap(OrderLineDto::beerId, Function.identity()));
			var updateOrderLine = new ArrayList<OrderLineDto>();
			collectionAsStream(__).forEach(o -> {
				var orderLine = map.get(o.getBeerId());
				var reqQty = orderLine.orderQuantity() - orderLine.quantityAllocated();
				var allocated = reqQty >= o.getQtyOnHand() ? o.getQtyOnHand() : o.getQtyOnHand() - reqQty;
				o.setQtyOnHand(o.getQtyOnHand() - allocated);
				updateOrderLine.add(new OrderLineDto(orderLine.beerId(), orderLine.orderQuantity(), allocated));
				inventoryList.add(o);
				inventoryLedgerList.add(InventoryLedger.builder().inventoryId(o.getId()).type(ALLOCATE).qty(allocated)
						.totQty(o.getQtyOnHand()).build());
			});

			var inventoryMono = inventoryRepository.saveAll(inventoryList);
			var inventoryLedgerMono = inventoryLedgerRepository.saveAll(inventoryLedgerList);
			return inventoryMono.thenMany(inventoryLedgerMono)
					.then(orderStatusProducer.send(
							new OrderEvent(uuid.get(),
									new OrderDto(value.orderDto().id(), null, updateOrderLine, OrderStatus.ALLOCATED)),
							Map.of()));
		});

		return validateHeaders.then(persist).onErrorResume(__ -> orderStatusProducer.send(new OrderEvent(uuid.get(),
				new OrderDto(value.orderDto().id(), null, value.orderDto().orderLine(), OrderStatus.ALLOCATION_ERROR)),
				Map.of()));

	}

}
