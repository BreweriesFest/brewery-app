package com.brewery.app.inventory.service;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.exception.BusinessException;
import com.brewery.app.inventory.mapper.InventoryMapper;
import com.brewery.app.inventory.repository.Inventory;
import com.brewery.app.inventory.repository.InventoryRepository;
import com.brewery.app.inventory.repository.QInventory;
import com.brewery.app.inventory.util.ValidationResult;
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
import reactor.core.scheduler.Schedulers;

import java.util.Collection;

import static com.brewery.app.exception.ExceptionReason.INTERNAL_SERVER_ERROR;
import static com.brewery.app.exception.ExceptionReason.INVALID_SHOPPING_LIST_ID;
import static com.brewery.app.inventory.util.Validator.validateInventoryDTO;
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
    private final Retry mongoServiceRetryCustomizer;

    public Mono<InventoryDTO> saveInventory(InventoryDTO inventoryDTO) {
        // reactiveMongoOperations.upsert();

        var validation = validateInventoryDTO(inventoryDTO).flatMap(validationResult -> {
            if (validationResult != ValidationResult.SUCCESS)
                return Mono.error(new RuntimeException(INVALID_SHOPPING_LIST_ID.getMessage()));
            return Mono.empty();
        });

        var persist = Mono.deferContextual(ctx -> {
            QInventory inventory = QInventory.inventory;
            return inventoryRepository.findOne(inventory.beerId.eq(inventoryDTO.beerId())
                    .and(inventory.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))));
        }).map(beerInventory -> inventoryMapper.fromInventoryDTO(inventoryDTO, beerInventory))
                .switchIfEmpty(Mono.just(inventoryMapper.fromInventoryDTO(inventoryDTO)))
                .flatMap(beerInventory -> inventoryRepository.save(beerInventory))
                .map(inventoryMapper::fromBeerInventory).transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("mongo");
                    return rcb.run(it.doFirst(() -> log.info("circuit breaker wrapper")),
                            throwable -> Mono.error(new RuntimeException(throwable.getMessage())));
                }).doOnError(exc -> log.error("exception", exc))
                .transformDeferred(RetryOperator.of(mongoServiceRetryCustomizer));

        return validation.then(persist).doOnError(exc -> log.error("exception", exc))
                // .onErrorResume(throwable -> Mono.error(new BadRequestException(throwable.getMessage())))
                // .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<InventoryDTO> inventoryByBeerId(Collection<String> beerId) {

        var validate = validateContext();

        var beerInventory = Flux.deferContextual(ctx -> {
            QInventory inventory = QInventory.inventory;
            return inventoryRepository.findAll(inventory.beerId.in(beerId)
                    .and(inventory.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))));
        }).switchIfEmpty(Flux.just(new Inventory())).map(inventoryMapper::fromBeerInventory)
                .onErrorReturn(new InventoryDTO(null, null, null)).transform(it -> {
                    var rcb = reactiveCircuitBreakerFactory.create("mongo");
                    return rcb.run(it, throwable -> {
                        log.error("exception::", throwable);
                        return Flux.error(new BusinessException(INTERNAL_SERVER_ERROR));
                    });
                }).transformDeferred(RetryOperator.of(mongoServiceRetryCustomizer));

        return validate.thenMany(beerInventory).onErrorReturn(new InventoryDTO(null, null, null));

    }
}
