package com.brewery.app.inventory.resource;

import com.brewery.app.inventory.domain.BeerInventory;
import com.brewery.app.inventory.repository.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    @GetMapping("/save")
    public Mono<BeerInventory> saveInventory() {
        log.info("inside controller {}", Thread.currentThread().getName());
        var uuidMono = uuid.get();
        uuidMono.flatMap(strg -> Mono.just(BeerInventory.builder().upc(strg).build()));
        return uuidMono.map(uuid -> BeerInventory.builder().upc("upc").beerId(uuid).quantityOnHand(10).build())
                .flatMap(beerInventoryRepository::save)
                .contextWrite(__ -> __.putAllMap(Map.of("tenantId", "shubham", "customerId", "goel")))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
