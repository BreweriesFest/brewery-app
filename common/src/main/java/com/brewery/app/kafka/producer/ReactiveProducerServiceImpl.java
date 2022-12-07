package com.brewery.app.kafka.producer;

import com.brewery.app.domain.Record;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.SenderResult;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;

@Slf4j
public abstract class ReactiveProducerServiceImpl<K, V extends Record<K>> extends ReactiveProducerConfig<K, V>
        implements ReactiveProducerService<K, V> {
    protected ReactiveProducerServiceImpl(KafkaProperties kafkaProperties, Class<?> serializer, Class<?> deSerializer,
            MeterRegistry meterRegistry) {
        super(kafkaProperties, serializer, deSerializer, meterRegistry);
    }

    @Override
    public Mono<SenderResult<Void>> send(V value, Map<String, Object> header) {
        // put tenant, banner, key in headers
        // get tenant banner from reactive context
        var clazz = (Class<V>) Arrays
                .stream(((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments())
                .reduce((first, second) -> second).orElse(null);

        return Mono.deferContextual(ctx -> Mono.just(MessageBuilder.withPayload(value)
                .copyHeaders(generateHeaders(value.key(), ctx.get(TENANT_ID), ctx.get(CUSTOMER_ID), header)).build()))
                .flatMap(msg -> send(msg)).subscribeOn(Schedulers.boundedElastic());

    }

    private Mono<SenderResult<Void>> send(Message<V> msg) {
        return reactiveKafkaProducerTemplate.send(topic, msg).publishOn(Schedulers.boundedElastic())
                .doOnSuccess(
                        senderResult -> log.info("sent {} offset : {}", msg, senderResult.recordMetadata().offset()))
                .doOnSubscribe(subs -> {
                    reactiveKafkaProducerTemplate.doOnProducer(producer -> {
                        micrometerProducerListener.producerAdded("myProducer", producer);
                        return Mono.empty();
                    }).subscribe();
                });
    }

    Map<String, Object> generateHeaders(K key, String tenantId, String customerId, Map<String, Object> customHeaders) {
        var header = new HashMap<String, Object>();
        header.put(KafkaHeaders.MESSAGE_KEY, key);
        header.put(TENANT_ID, tenantId);
        header.put(CUSTOMER_ID, customerId);
        header.putAll(customHeaders);
        return header;
    }
}
