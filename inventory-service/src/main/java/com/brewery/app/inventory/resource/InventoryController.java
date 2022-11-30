package com.brewery.app.inventory.resource;

import com.brewery.app.inventory.repository.BeerInventory;
import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.inventory.repository.BeerInventoryRepository;
import com.brewery.app.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    public static final Supplier<Mono<String>> uuid = () -> Mono.just(UUID.randomUUID().toString())
            .subscribeOn(Schedulers.boundedElastic());
    private final BeerInventoryRepository beerInventoryRepository;
    private final TransactionalOperator transactionalOperator;

    private final InventoryService inventoryService;

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
    Mono<String> hello() {
        return Mono.just("hello graphql");
    }

    @QueryMapping
    Mono<String> helloWithName(@Argument String name) {
        return Mono.just("hello " + name + "!");
    }

    @QueryMapping
    Mono<BeerInventory> inventory() {
        return Mono.just(
                BeerInventory.builder().beerId(UUID.randomUUID().toString()).upc("upc").quantityOnHand(100001).build());
    }

    @QueryMapping
    Flux<BeerInventory> inventories() {
        return Flux.fromIterable(List.of(
                BeerInventory.builder().beerId(UUID.randomUUID().toString()).upc("upc1").quantityOnHand(100001).build(),
                BeerInventory.builder().beerId(UUID.randomUUID().toString()).upc("upc2").quantityOnHand(100002)
                        .build()));
    }

    @BatchMapping
    Mono<Map<BeerInventory, Beer>> beer(List<BeerInventory> beerInventories) {
        return Mono.just(beerInventories.stream()
                .collect(Collectors.toMap(inventory -> inventory, inventory -> new Beer(inventory.getBeerId()))));
    }

    ;

    @MutationMapping
    Mono<InventoryDTO> addInventory(@Argument InventoryDTO inventory) {
        return inventoryService.saveInventory(inventory);
    }

    record Beer(String beerId) {
    }
}
