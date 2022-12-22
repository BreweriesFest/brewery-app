package com.brewery.app.kafka.consumer;

import com.brewery.app.domain.Record;
import com.brewery.app.properties.kafka.KafkaConsumerProps;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.Helper.extractHeaders;

@Getter
@Slf4j
public abstract class ReactiveConsumerConfig<K, V extends Record<K>> {

    protected final ReactiveKafkaConsumerTemplate<K, V> reactiveKafkaConsumerTemplate;
    protected final MicrometerConsumerListener<K, V> micrometerConsumerListener;

    protected ReactiveConsumerConfig(KafkaConsumerProps kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        var kafkaReceiverOptions = kafkaReceiverOptions(kafkaProperties, serializer, deSerializer);
        micrometerConsumerListener = new MicrometerConsumerListener<>(meterRegistry);
        reactiveKafkaConsumerTemplate = new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);

    }

    public Flux<ReceiverRecord<K, V>> consumerRecord(Function<ReceiverRecord<K, V>, Mono<ReceiverRecord<K, V>>> input) {
        return reactiveKafkaConsumerTemplate
                // .assignment().flatMap(a->reactiveKafkaConsumerTemplate.resume(a)).subscribe()
                .receive().publishOn(Schedulers.boundedElastic())
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> {
                    log.info("received key={}, value={} from topic={}, offset={}", consumerRecord.key(),
                            consumerRecord.value(), consumerRecord.topic(), consumerRecord.offset());
                })
                // .map(ConsumerRecord::value)
                .flatMap(rec -> input.apply(rec)
                        .contextWrite(__ -> __.putAllMap(extractHeaders(List.of(TENANT_ID, CUSTOMER_ID), rec))))
                .doOnSubscribe(subs -> {
                    reactiveKafkaConsumerTemplate.doOnConsumer(consumer -> {
                        micrometerConsumerListener.consumerAdded("myConsumer", consumer);
                        return Mono.empty();
                    }).subscribe();
                }).doOnNext(receiverRecordFlux -> receiverRecordFlux.receiverOffset().acknowledge())
                // .doOnNext(fakeConsumerDTO -> log.info("successfully consumed {}={}",
                // InventoryDTO.class.getSimpleName(),
                // fakeConsumerDTO))
                .doOnError(
                        throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()))
                .retry(2);

    }

    private ReceiverOptions<K, V> kafkaReceiverOptions(KafkaConsumerProps kafkaProperties, Class<?> serializer,
            Class<?> deSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, serializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deSerializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumerGroup());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return ReceiverOptions.<K, V> create(props)
                .addAssignListener(receiverPartitions -> log.info("assigned partition {}", receiverPartitions))
                .addRevokeListener(receiverPartitions -> log.info("revoke partition {}", receiverPartitions))
                .subscription(Collections.singletonList(kafkaProperties.getTopic()));
    }

}
