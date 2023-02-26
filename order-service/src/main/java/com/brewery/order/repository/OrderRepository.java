package com.brewery.order.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

public interface OrderRepository
		extends ReactiveMongoRepository<Order, String>, ReactiveQuerydslPredicateExecutor<Order> {

}
