package com.brewery.app.kafka.producer;

import com.brewery.app.domain.Record;

import java.util.Map;

public interface ReactiveProducerService<K, V extends Record<K>> {
    void send(V value, Map<String, Object> header);

}
