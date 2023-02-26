package com.brewery.scheduler.config;

import com.brewery.common.kafka.producer.ReactiveProducerService;
import com.brewery.common.kafka.producer.ReactiveProducerServiceImpl;
import com.brewery.model.event.CheckInventoryEvent;
import com.brewery.model.properties.kafka.KafkaProducerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

	@Bean
	@ConfigurationProperties(prefix = "app.kafka.producer")
	public KafkaProducerProps kafkaProducerProps() {
		return new KafkaProducerProps();
	}

	@Bean
	public ReactiveProducerService<String, CheckInventoryEvent> reactiveProducer(KafkaProducerProps properties,
			MeterRegistry meterRegistry) {
		return new ReactiveProducerServiceImpl<>(properties, StringSerializer.class, JsonSerializer.class,
				meterRegistry) {
		};
	}

}