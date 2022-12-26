package com.brewery.app.scheduler.config;

import com.brewery.app.event.CheckInventoryEvent;
import com.brewery.app.kafka.producer.ReactiveProducerService;
import com.brewery.app.kafka.producer.ReactiveProducerServiceImpl;
import com.brewery.app.properties.kafka.KafkaProducerProps;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;

import static com.brewery.app.util.AppConstant.LZ4_COMPRESSION;

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

    @Bean
    public NewTopic newUserTopic(KafkaProducerProps kafkaProducerProps) {
        return TopicBuilder.name(kafkaProducerProps.getTopic()).partitions(3).replicas(1)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, LZ4_COMPRESSION).build();

    }
}