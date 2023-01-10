package com.brewery.app.config;

import com.mongodb.WriteConcern;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class MongoDBConfig {

	@Bean
	@Profile("mongo-reactive")
	public ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory factory) {
		return new ReactiveMongoTransactionManager(factory);
	}

	@Bean
	@Profile("mongo-reactive")
	public TransactionalOperator transactionalOperator(ReactiveTransactionManager txm) {
		return TransactionalOperator.create(txm);
	}

	@Bean
	@Profile({ "mongo-sync", "mongo-reactive" })
	public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
		return builder -> builder.writeConcern(WriteConcern.ACKNOWLEDGED)
				.applyToConnectionPoolSettings(builder1 -> builder1.minSize(10));
	}

}
