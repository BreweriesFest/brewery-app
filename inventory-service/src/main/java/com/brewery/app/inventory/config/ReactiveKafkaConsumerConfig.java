package com.brewery.app.inventory.config;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.kafka.ReactiveConsumerConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;

@Configuration
public class ReactiveKafkaConsumerConfig {
    @Bean
    public Flux<InventoryDTO> reactiveKafkaConsumer(KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
        return new ReactiveConsumerConfig<String, InventoryDTO>(kafkaProperties, StringDeserializer.class,
                JsonDeserializer.class, meterRegistry) {
        }.getConsumedRecord();
    }
}
