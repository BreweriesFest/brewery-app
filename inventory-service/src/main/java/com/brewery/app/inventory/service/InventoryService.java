package com.brewery.app.inventory.service;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.inventory.mapper.InventoryMapper;
import com.brewery.app.inventory.repository.BeerInventoryRepository;
import com.brewery.app.inventory.repository.QBeerInventory;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.brewery.app.exception.ExceptionReason.INVALID_SHOPPING_LIST_ID;
import static com.brewery.app.inventory.util.Validator.validateInventoryDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final BeerInventoryRepository beerInventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final TransactionalOperator transactionalOperator;
    private final ReactiveMongoOperations reactiveMongoOperations;
    private final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;
    private final Retry retry;

    public Mono<InventoryDTO> saveInventory(InventoryDTO inventoryDTO) {
        // reactiveMongoOperations.upsert();

        var validation = validateInventoryDTO(inventoryDTO).flatMap(validationResult -> {
            if (validationResult != ValidationResult.SUCCESS)
                return Mono.error(new RuntimeException(INVALID_SHOPPING_LIST_ID.getMessage()));
            return Mono.empty();
        });

        var persist = Mono.deferContextual(ctx -> {
            QBeerInventory inventory = QBeerInventory.beerInventory;
            return beerInventoryRepository
                    .findOne(inventory.beerId.eq(inventoryDTO.beerId()).and(inventory.upc.eq(inventoryDTO.upc()))
                            .and(inventory.tenantId.eq((String) ctx.get("tenantId"))));
        }).map(beerInventory -> inventoryMapper.fromInventoryDTO(inventoryDTO, beerInventory))
                .switchIfEmpty(Mono.just(inventoryMapper.fromInventoryDTO(inventoryDTO)))
                .flatMap(beerInventory -> beerInventoryRepository.save(beerInventory))
                .map(inventoryMapper::fromBeerInventory).transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("mongo");
                    return rcb.run(it.doFirst(() -> log.info("circuit breaker wrapper")),
                            throwable -> Mono.error(new RuntimeException(throwable.getMessage())));
                }).doOnError(exc -> log.error("exception", exc))
                .transformDeferred(RetryOperator.of(Retry.ofDefaults("mongodb")));

        return validation.then(persist).doOnError(exc -> log.error("exception", exc))
                // .onErrorResume(throwable -> Mono.error(new BadRequestException(throwable.getMessage())))
                // .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
