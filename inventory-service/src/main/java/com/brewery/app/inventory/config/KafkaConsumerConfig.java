package com.brewery.app.inventory.config;

import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.kafka.consumer.ReactiveConsumerConfig;
import com.brewery.app.properties.kafka.KafkaConsumerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.kafka.inventory.consumer")
    public KafkaConsumerProps kafkaConsumerProps() {
        return new KafkaConsumerProps();
    }

    @Bean
    public ReactiveConsumerConfig<String, BrewBeerEvent> reactiveConsumer(KafkaConsumerProps kafkaConsumerProps,
            MeterRegistry meterRegistry) {
        return new ReactiveConsumerConfig<>(kafkaConsumerProps, StringDeserializer.class, JsonDeserializer.class,
                meterRegistry) {
        };
    }
}
