package com.brewery.app.inventory.config;

import com.brewery.app.domain.InventoryDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
public class ReactiveKafkaConsumerConfig {

    @Bean
    public ReceiverOptions<String, InventoryDTO> kafkaReceiverOptions(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumer_group");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ReceiverOptions<String, InventoryDTO> basicReceiverOptions = ReceiverOptions.create(props);
        return basicReceiverOptions.subscription(Collections.singletonList("test"));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, InventoryDTO> reactiveKafkaConsumerTemplate(
            ReceiverOptions<String, InventoryDTO> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}
