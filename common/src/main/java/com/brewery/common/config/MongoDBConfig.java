package com.brewery.common.config;

import com.mongodb.WriteConcern;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration(proxyBeanMethods = false)
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
	public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(ObservationRegistry registry) {
		return builder -> builder.contextProvider(ContextProviderFactory.create(registry))
			.addCommandListener(new MongoObservationCommandListener(registry))
			.writeConcern(WriteConcern.ACKNOWLEDGED)
			.applyToConnectionPoolSettings(builder1 -> builder1.minSize(10));
	}

}
