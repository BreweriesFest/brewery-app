package com.brewery.app.kafka.producer;

import com.brewery.app.domain.Record;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.Map;

public interface ReactiveProducerService<K, V extends Record<K>> {
    Mono<SenderResult<Void>> send(V value, Map<String, Object> header);

}
