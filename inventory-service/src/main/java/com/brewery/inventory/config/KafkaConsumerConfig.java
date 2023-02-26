package com.brewery.inventory.config;

import com.brewery.common.kafka.consumer.ReactiveConsumerConfig;
import com.brewery.model.event.BrewBeerEvent;
import com.brewery.model.event.OrderEvent;
import com.brewery.model.properties.kafka.KafkaConsumerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import static com.brewery.common.util.AppConstant.LZ4_COMPRESSION;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	@ConfigurationProperties(prefix = "app.kafka.brew-beer-consumer")
	public KafkaConsumerProps brewBeerConsumerProps() {
		return new KafkaConsumerProps();
	}

	@Bean
	@ConfigurationProperties(prefix = "app.kafka.allocate-beer-consumer")
	public KafkaConsumerProps allocateBeerConsumerProps() {
		return new KafkaConsumerProps();
	}

	@Bean
	public ReactiveConsumerConfig<String, BrewBeerEvent> reactiveConsumer(KafkaConsumerProps brewBeerConsumerProps,
			MeterRegistry meterRegistry) {
		return new ReactiveConsumerConfig<>(brewBeerConsumerProps, StringDeserializer.class, JsonDeserializer.class,
				meterRegistry) {
		};
	}

	@Bean
	public ReactiveConsumerConfig<String, OrderEvent> reactiveAllocateBeerConsumer(
			KafkaConsumerProps allocateBeerConsumerProps, MeterRegistry meterRegistry) {
		return new ReactiveConsumerConfig<>(allocateBeerConsumerProps, StringDeserializer.class, JsonDeserializer.class,
				meterRegistry) {
		};
	}

	@Bean
	public NewTopic brewBeerTopic(KafkaConsumerProps brewBeerConsumerProps) {
		return TopicBuilder.name(brewBeerConsumerProps.getTopic()).partitions(3).replicas(1)
				.config(TopicConfig.COMPRESSION_TYPE_CONFIG, LZ4_COMPRESSION).build();

	}

	@Bean
	public NewTopic allocateBeerTopic(KafkaConsumerProps allocateBeerConsumerProps) {
		return TopicBuilder.name(allocateBeerConsumerProps.getTopic()).partitions(3).replicas(1)
				.config(TopicConfig.COMPRESSION_TYPE_CONFIG, LZ4_COMPRESSION).build();

	}

}
