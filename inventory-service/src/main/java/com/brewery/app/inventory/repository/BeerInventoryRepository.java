package com.brewery.app.inventory.repository;

import com.brewery.app.inventory.domain.BeerInventory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BeerInventoryRepository extends ReactiveMongoRepository<BeerInventory, String> {
}
