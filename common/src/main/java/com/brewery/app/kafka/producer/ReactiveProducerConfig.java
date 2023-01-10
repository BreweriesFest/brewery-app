package com.brewery.app.kafka.producer;

import com.brewery.app.domain.Record;
import com.brewery.app.properties.kafka.KafkaProducerProps;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

import static com.brewery.app.util.AppConstant.LZ4_COMPRESSION;

@Slf4j
@Getter
public abstract class ReactiveProducerConfig<K, V extends Record<K>> {

	protected final ReactiveKafkaProducerTemplate<K, V> reactiveKafkaProducerTemplate;

	protected final MicrometerProducerListener<K, V> micrometerProducerListener;

	protected final String topic;

	ReactiveProducerConfig(KafkaProducerProps kafkaProperties, Class<?> serializer, Class<?> deSerializer,
			MeterRegistry meterRegistry) {
		this.micrometerProducerListener = new MicrometerProducerListener<>(meterRegistry);
		this.topic = kafkaProperties.getTopic();
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializer);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, deSerializer);
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaProperties.isIdempotence());
		props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, LZ4_COMPRESSION);
		this.reactiveKafkaProducerTemplate = new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));

	}

}
