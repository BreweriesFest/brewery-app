package com.brewery.app.inventory.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

public interface BeerInventoryRepository
        extends ReactiveMongoRepository<BeerInventory, String>, ReactiveQuerydslPredicateExecutor<BeerInventory> {

}
