package com.brewery.app.inventory.service;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.exception.BusinessException;
import com.brewery.app.inventory.mapper.InventoryMapper;
import com.brewery.app.inventory.repository.InventoryRepository;
import com.brewery.app.inventory.repository.QInventory;
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

import java.util.Collection;

import static com.brewery.app.exception.ExceptionReason.CUSTOMIZE_REASON;
import static com.brewery.app.exception.ExceptionReason.INTERNAL_SERVER_ERROR;
import static com.brewery.app.inventory.util.ValidationResult.SUCCESS;
import static com.brewery.app.inventory.util.Validator.validateInventoryDTO;
import static com.brewery.app.util.AppConstant.RESILIENCE_ID_MONGO;
import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.Helper.fetchHeaderFromContext;
import static com.brewery.app.util.Helper.validateContext;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final TransactionalOperator transactionalOperator;
    private final ReactiveMongoOperations reactiveMongoOperations;
    private final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;
    private final Retry mongoServiceRetry;

    public Mono<InventoryDTO> addInventory(BrewBeerEvent brewBeerEvent) {
        // reactiveMongoOperations.upsert();

        var validateHeaders = validateContext();

        var validateRequest = validateInventoryDTO(brewBeerEvent).flatMap(result -> SUCCESS.equals(result)
                ? Mono.empty() : Mono.error(new BusinessException(CUSTOMIZE_REASON, result.name())));

        var persist = Mono.deferContextual(ctx -> {
            QInventory inventory = QInventory.inventory;
            return inventoryRepository.findOne(inventory.beerId.eq(brewBeerEvent.beerId())
                    .and(inventory.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))));
        }).map(inventory -> {
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() + brewBeerEvent.qtyToBrew());
            return inventory;
        }).switchIfEmpty(Mono.just(inventoryMapper.fromBrewBeerEvent(brewBeerEvent))).flatMap(inventoryRepository::save)
                .map(inventoryMapper::fromInventory).transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create(RESILIENCE_ID_MONGO);
                    return rcb.run(it, throwable -> {
                        log.error("exception::", throwable);
                        return Mono.error(new BusinessException(INTERNAL_SERVER_ERROR));
                    });
                })
                // .as(transactionalOperator::transactional)
                .transformDeferred(RetryOperator.of(mongoServiceRetry));

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
}
