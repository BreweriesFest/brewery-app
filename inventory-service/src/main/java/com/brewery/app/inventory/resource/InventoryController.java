package com.brewery.app.inventory.resource;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final InventoryService inventoryService;

    @QueryMapping
    public Flux<InventoryDTO> inventory(@Argument Collection<String> beerId) {
        return inventoryService.inventoryByBeerId(beerId);
    }

    @MutationMapping
    public Mono<InventoryDTO> addInventory(@Argument BrewBeerEvent inventory) {
        return inventoryService.addInventory(inventory);
    }
}
