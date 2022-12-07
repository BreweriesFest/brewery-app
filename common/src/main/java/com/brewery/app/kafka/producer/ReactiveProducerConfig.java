package com.brewery.app.kafka.producer;

import com.brewery.app.domain.Record;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

public abstract class ReactiveProducerConfig<K, V extends Record<K>> {

    protected final ReactiveKafkaProducerTemplate<K, V> reactiveKafkaProducerTemplate;
    protected final MicrometerProducerListener<K, V> micrometerProducerListener;
    protected final String topic;

    ReactiveProducerConfig(KafkaProperties kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        this.micrometerProducerListener = new MicrometerProducerListener<>(meterRegistry);
        this.topic = "test";
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, deSerializer);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        this.reactiveKafkaProducerTemplate = new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));

    }
}
