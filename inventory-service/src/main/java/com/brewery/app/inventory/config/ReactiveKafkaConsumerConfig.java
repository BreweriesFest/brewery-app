package com.brewery.app.inventory.config;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.kafka.consumer.ReactiveConsumerConfig;
import com.brewery.app.properties.kafka.KafkaConsumerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class ReactiveKafkaConsumerConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.kafka.inventory.consumer")
    public KafkaConsumerProps kafkaConsumerProps() {
        return new KafkaConsumerProps();
    }

    @Bean
    public ReactiveConsumerConfig<String, InventoryDTO> reactiveConsumer(KafkaConsumerProps kafkaConsumerProps,
            MeterRegistry meterRegistry) {
        return new ReactiveConsumerConfig<>(kafkaConsumerProps, StringDeserializer.class, JsonDeserializer.class,
                meterRegistry) {
        };
    }
}
