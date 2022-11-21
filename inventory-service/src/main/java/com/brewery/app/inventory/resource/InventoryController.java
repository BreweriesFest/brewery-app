package com.brewery.app.inventory.resource;

import com.brewery.app.inventory.domain.BeerInventory;
import com.brewery.app.inventory.repository.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    public static final Supplier<Mono<String>> uuid = () -> Mono.just(UUID.randomUUID().toString())
            .subscribeOn(Schedulers.boundedElastic());
    private final BeerInventoryRepository beerInventoryRepository;
    private final TransactionalOperator transactionalOperator;

    @GetMapping("/save")
    public Mono<BeerInventory> saveInventory() {
        log.info("inside controller {}", Thread.currentThread().getName());
        var uuidMono = uuid.get();
        uuidMono.flatMap(strg -> Mono.just(BeerInventory.builder().upc(strg).build()));
        return uuidMono.map(uuid -> BeerInventory.builder().upc("upc").beerId(uuid).quantityOnHand(10).build())
                .flatMap(beerInventoryRepository::save)
                .contextWrite(__ -> __.putAllMap(Map.of("tenantId", "shubham", "customerId", "goel")))
                .as(transactionalOperator::transactional).subscribeOn(Schedulers.boundedElastic());
    }

    @QueryMapping
    String hello() {
        return "hello graphql";
    }

    @QueryMapping
    String helloWithName(@Argument String name) {
        return "hello " + name + "!";
    }

    @QueryMapping
    BeerInventory inventory() {
        return BeerInventory.builder().beerId(UUID.randomUUID().toString()).upc("upc").quantityOnHand(100001).build();
    }

    @QueryMapping
    Flux<BeerInventory> inventories() {
        return Flux.fromIterable(List.of(
                BeerInventory.builder().beerId(UUID.randomUUID().toString()).upc("upc1").quantityOnHand(100001).build(),
                BeerInventory.builder().beerId(UUID.randomUUID().toString()).upc("upc2").quantityOnHand(100002)
                        .build()));
    }
}
