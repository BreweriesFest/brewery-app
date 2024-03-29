package com.brewery.order.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

public interface OrderLineRepository
		extends ReactiveMongoRepository<OrderLine, String>, ReactiveQuerydslPredicateExecutor<OrderLine> {

	// Flux<OrderLine> findAllByOrderIdIn(List<ObjectId> o);

}
