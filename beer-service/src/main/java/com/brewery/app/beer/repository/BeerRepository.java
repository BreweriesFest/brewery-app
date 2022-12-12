package com.brewery.app.beer.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

public interface BeerRepository extends ReactiveMongoRepository<Beer, String>, ReactiveQuerydslPredicateExecutor<Beer> {
}
