package com.brewery.common.kafka.producer;

import com.brewery.model.domain.Record;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.Map;

public interface ReactiveProducerService<K, V extends Record<K>> {

	Mono<SenderResult<Void>> send(V value, Map<String, Object> header);

}
