package com.brewery.app.kafka.producer;

import com.brewery.app.domain.Record;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public abstract class ReactiveProducerServiceImpl<K, V extends Record<K>> extends ReactiveProducerConfig<K, V>
        implements ReactiveProducerService<K, V> {
    protected ReactiveProducerServiceImpl(KafkaProperties kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        super(kafkaProperties, serializer, deSerializer, meterRegistry);
    }

    @Override
    public void send(V value, Map<String, Object> header) {
        // put tenant, banner, key in headers
        // get tenant banner from reactive context
        var clazz = (Class<V>) Arrays
                .stream(((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments())
                .reduce((first, second) -> second).orElse(null);
        var msg = MessageBuilder.withPayload(value).copyHeaders(header).build();
        MicrometerProducerListener<K, V> micro = new MicrometerProducerListener<>(meterRegistry);
        log.info("send to topic={}, {},", topic, msg);
        reactiveKafkaProducerTemplate.send(topic, msg).publishOn(Schedulers.boundedElastic())
                .doOnSuccess(
                        senderResult -> log.info("sent {} offset : {}", msg, senderResult.recordMetadata().offset()))
                .doOnSubscribe(subs -> {
                    reactiveKafkaProducerTemplate.doOnProducer(producer -> {
                        micro.producerAdded("myProducer", producer);
                        return Mono.empty();
                    }).subscribe();
                }).subscribe();
    }
}
