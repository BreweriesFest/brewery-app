package com.brewery.app.inventory.config;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.kafka.producer.ReactiveProducerService;
import com.brewery.app.kafka.producer.ReactiveProducerServiceImpl;
import com.brewery.app.properties.kafka.KafkaProducerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class ReactiveKafkaProducerConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.kafka.inventory.producer")
    public KafkaProducerProps kafkaProducerProps() {
        return new KafkaProducerProps();
    }

    @Bean
    public ReactiveProducerService<String, InventoryDTO> reactiveProducerService(KafkaProducerProps properties,
            MeterRegistry meterRegistry, KafkaProperties kafkaProperties) {
        var props = kafkaProperties.buildProducerProperties();
        return new ReactiveProducerServiceImpl<>(properties, StringSerializer.class, JsonSerializer.class,
                meterRegistry) {
        };
    }
}
