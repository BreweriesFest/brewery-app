package com.brewery.app.kafka.consumer;

import com.brewery.app.domain.Record;
import com.brewery.app.properties.kafka.KafkaConsumerProps;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
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

    protected final String topic;

    protected final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

    protected ReactiveConsumerConfig(KafkaConsumerProps kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        this.topic = kafkaProperties.getTopic();
        var kafkaReceiverOptions = kafkaReceiverOptions(kafkaProperties, serializer, deSerializer);
        micrometerConsumerListener = new MicrometerConsumerListener<>(meterRegistry);
        reactiveKafkaConsumerTemplate = new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
        deadLetterPublishingRecoverer = deadLetterPublishingRecoverer();

    }

    public Flux<ReceiverRecord<K, V>> consumerRecord(Function<ReceiverRecord<K, V>, Mono<?>> input) {
        return reactiveKafkaConsumerTemplate
                // .assignment().flatMap(a->reactiveKafkaConsumerTemplate.resume(a))

                .receive().publishOn(Schedulers.boundedElastic())
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> {
                    log.info("received key={}, value={}, headers={} from topic={}, partition={}, offset={}",
                            consumerRecord.key(), consumerRecord.value(), consumerRecord.headers().toArray(),
                            consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
                })
                .flatMap(__ -> input.apply(__)
                        .contextWrite(ctx -> ctx.putAllMap(extractHeaders(List.of(TENANT_ID, CUSTOMER_ID), __)))
                        .map(___ -> __).onErrorResume(error -> Mono.error(new ReceiverRecordException(__, error)))
                        .onErrorStop())
                // .map(__-> Tuples.of(__,Mono.just(__),input.apply(__)
                // .contextWrite(ctx -> ctx.putAllMap(extractHeaders(List.of(TENANT_ID, CUSTOMER_ID), __)))))
                // .flatMap(___->Mono.zip(___.getT3(),___.getT2(),(o1,o2)->{
                // ___.getT1().receiverOffset().acknowledge();
                // return o2;
                // }).onErrorResume(error->Mono.error(new ReceiverRecordException(___.getT1(),error))))
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2)).transientErrors(true).onRetryExhaustedThrow((a, b) -> {
                            log.info("testing");
                            return b.failure();
                        }))

                .onErrorContinue((e, o) -> {
                    ReceiverRecordException ex = (ReceiverRecordException) e;
                    System.out.println("Retries exhausted for " + ex);
                    // deadLetterPublishingRecoverer.accept(ex.getRecord(), ex);
                    ex.getRecord().receiverOffset().acknowledge();
                }).doOnSubscribe(subs -> {
                    reactiveKafkaConsumerTemplate.doOnConsumer(consumer -> {
                        micrometerConsumerListener.consumerAdded("consuming::" + topic, consumer);
                        return Mono.empty();
                    }).subscribe();
                }).repeat();

    }

    private ReceiverOptions<K, V> kafkaReceiverOptions(KafkaConsumerProps kafkaProperties, Class<?> serializer,
            Class<?> deSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, serializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deSerializer);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumerGroup());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return ReceiverOptions.<K, V> create(props).commitInterval(Duration.ZERO).commitBatchSize(0)
                .addAssignListener(receiverPartitions -> log.info("assigned partition {}", receiverPartitions))
                .addRevokeListener(receiverPartitions -> log.info("revoke partition {}", receiverPartitions))
                .subscription(Collections.singletonList(kafkaProperties.getTopic()));
    }

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
