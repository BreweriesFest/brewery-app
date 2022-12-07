package com.brewery.app.inventory.config;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.kafka.consumer.ReactiveConsumerConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

@Configuration
public class ReactiveKafkaConsumerConfig {
    @Bean
    public Flux<ReceiverRecord<String, InventoryDTO>> receiverRecordFlux(KafkaProperties kafkaProperties,
            MeterRegistry meterRegistry) {
        return new ReactiveConsumerConfig<String, InventoryDTO>(kafkaProperties, StringDeserializer.class,
                JsonDeserializer.class, meterRegistry) {
        }.getReceiverRecordFlux();
    }
}
