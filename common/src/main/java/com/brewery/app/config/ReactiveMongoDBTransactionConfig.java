package com.brewery.app.config;

import com.mongodb.WriteConcern;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class ReactiveMongoDBTransactionConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory factory) {
        return new ReactiveMongoTransactionManager(factory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager txm) {
        return TransactionalOperator.create(txm);
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(MeterRegistry meterRegistry) {
        return builder -> builder.writeConcern(WriteConcern.ACKNOWLEDGED)
                .applyToConnectionPoolSettings(builder1 -> builder1.minSize(5));

    }
}
