package com.brewery.app.inventory.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

public interface InventoryRepository
        extends ReactiveMongoRepository<Inventory, String>, ReactiveQuerydslPredicateExecutor<Inventory> {

}
