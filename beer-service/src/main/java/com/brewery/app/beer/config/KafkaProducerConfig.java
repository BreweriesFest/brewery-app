package com.brewery.app.beer.config;

import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.kafka.producer.ReactiveProducerService;
import com.brewery.app.kafka.producer.ReactiveProducerServiceImpl;
import com.brewery.app.properties.kafka.KafkaProducerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

	@Bean
	@ConfigurationProperties(prefix = "app.kafka.beer.producer")
	public KafkaProducerProps kafkaProducerProps() {
		return new KafkaProducerProps();
	}

	@Bean
	public ReactiveProducerService<String, BrewBeerEvent> reactiveProducerService(KafkaProducerProps properties,
			MeterRegistry meterRegistry) {
		return new ReactiveProducerServiceImpl<>(properties, StringSerializer.class, JsonSerializer.class,
				meterRegistry) {
		};
	}

}
