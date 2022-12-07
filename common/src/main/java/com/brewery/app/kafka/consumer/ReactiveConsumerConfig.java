package com.brewery.app.kafka.consumer;

import com.brewery.app.domain.Record;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.Map;

@Getter
@Slf4j
public abstract class ReactiveConsumerConfig<K, V extends Record<K>> {

    protected final ReactiveKafkaConsumerTemplate<K, V> reactiveKafkaConsumerTemplate;

    protected final Flux<ReceiverRecord<K, V>> receiverRecordFlux;

    protected ReactiveConsumerConfig(KafkaProperties kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        var kafkaReceiverOptions = kafkaReceiverOptions(kafkaProperties, serializer, deSerializer);
        MicrometerConsumerListener<K, V> micro = new MicrometerConsumerListener<K, V>(meterRegistry);
        reactiveKafkaConsumerTemplate = new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);

        receiverRecordFlux = reactiveKafkaConsumerTemplate
                // .assignment().flatMap(a->reactiveKafkaConsumerTemplate.resume(a)).subscribe()
                .receive()
                // .publishOn(Schedulers.boundedElastic())
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.offset()))
                // .map(ConsumerRecord::value)
                .doOnSubscribe(subs -> {
                    reactiveKafkaConsumerTemplate.doOnConsumer(consumer -> {
                        micro.consumerAdded("myConsumer", consumer);
                        return Mono.empty();
                    }).subscribe();
                })
                // .doOnNext(fakeConsumerDTO -> log.info("successfully consumed {}={}",
                // InventoryDTO.class.getSimpleName(),
                // fakeConsumerDTO))
                .doOnError(
                        throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()))
                .retry(2);

    }

    private ReceiverOptions<K, V> kafkaReceiverOptions(KafkaProperties kafkaProperties, Class<?> serializer,
            Class<?> deSerializer) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, serializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deSerializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test_consumer_group");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return ReceiverOptions.<K, V> create(props)
                .addAssignListener(receiverPartitions -> log.info("assigned partition {}", receiverPartitions))
                .addRevokeListener(receiverPartitions -> log.info("revoke partition {}", receiverPartitions))
                .subscription(Collections.singletonList("test"));
    }

}
