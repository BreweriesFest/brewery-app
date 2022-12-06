package com.brewery.app.kafka;

import com.brewery.app.domain.InventoryDTO;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.Map;

@Getter
@Slf4j
public abstract class ReactiveConsumerConfig<K, V> {

    protected final ReactiveKafkaConsumerTemplate<K, V> reactiveKafkaConsumerTemplate;

    protected final Flux<V> consumedRecord;

    protected ReactiveConsumerConfig(KafkaProperties kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        var kafkaReceiverOptions = kafkaReceiverOptions(kafkaProperties, serializer, deSerializer);
        MicrometerConsumerListener<K, V> micro = new MicrometerConsumerListener<K, V>(meterRegistry);
        reactiveKafkaConsumerTemplate = new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);

        consumedRecord = reactiveKafkaConsumerTemplate
                // .assignment().flatMap(a->reactiveKafkaConsumerTemplate.resume(a)).subscribe()
                .receiveAutoAck().publishOn(Schedulers.boundedElastic())
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.offset()))
                .map(ConsumerRecord::value).doOnSubscribe(subs -> {
                    reactiveKafkaConsumerTemplate.doOnConsumer(consumer -> {
                        micro.consumerAdded("myConsumer", consumer);
                        return Mono.empty();
                    }).subscribe();
                })
                .doOnNext(fakeConsumerDTO -> log.info("successfully consumed {}={}", InventoryDTO.class.getSimpleName(),
                        fakeConsumerDTO))
                .doOnError(
                        throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()));

    }

    private ReceiverOptions<K, V> kafkaReceiverOptions(KafkaProperties kafkaProperties, Class<?> serializer,
            Class<?> deSerializer) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, serializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deSerializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumer_group");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return ReceiverOptions.<K, V> create(props).subscription(Collections.singletonList("test"));
    }

}
