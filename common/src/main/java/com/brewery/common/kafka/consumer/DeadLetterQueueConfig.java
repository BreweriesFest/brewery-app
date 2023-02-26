package com.brewery.common.kafka.consumer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

public class DeadLetterQueueConfig<K, V> {

	private DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
		return new DeadLetterPublishingRecoverer(getEventKafkaTemplate(),
				(cr, e) -> new TopicPartition(cr.topic() + "_dlt", 0));
	}

	private KafkaOperations<K, V> getEventKafkaTemplate() {
		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
	}

	Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return props;
	}

}