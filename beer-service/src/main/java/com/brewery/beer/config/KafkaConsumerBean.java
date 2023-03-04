package com.brewery.beer.config;

import com.brewery.common.kafka.consumer.ReactiveConsumerConfig;
import com.brewery.model.event.CheckInventoryEvent;
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
public class KafkaConsumerBean {

	@Bean
	@ConfigurationProperties(prefix = "app.kafka.beer.consumer")
	public KafkaConsumerProps kafkaConsumerProps() {
		return new KafkaConsumerProps();
	}

	@Bean
	public ReactiveConsumerConfig<String, CheckInventoryEvent> reactiveConsumer(KafkaConsumerProps kafkaConsumerProps,
			MeterRegistry meterRegistry) {
		return new ReactiveConsumerConfig<>(kafkaConsumerProps, StringDeserializer.class, JsonDeserializer.class,
				meterRegistry) {
		};
	}

	@Bean
	public NewTopic newUserTopic(KafkaConsumerProps kafkaConsumerProps) {
		return TopicBuilder.name(kafkaConsumerProps.getTopic())
			.partitions(3)
			.replicas(1)
			.config(TopicConfig.COMPRESSION_TYPE_CONFIG, LZ4_COMPRESSION)
			.build();

	}

	@Bean
	public NewTopic deadLetterTopic(KafkaConsumerProps kafkaConsumerProps) {
		// https://docs.spring.io/spring-kafka/docs/2.8.2/reference/html/#configuring-topics
		return TopicBuilder.name(kafkaConsumerProps.getTopic() + "_dlt")
			// Use only one partition for infrequently used Dead Letter Topic
			.partitions(1)
			.build();
	}

}
