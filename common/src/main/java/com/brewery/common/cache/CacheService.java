package com.brewery.common.cache;

import com.brewery.model.domain.Record;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface CacheService<K, V extends Record<K>> {

	Mono<Map<K, V>> get(Collection<K> keys);

	Mono<V> get(K key);

	Mono<Boolean> put(Map<K, V> values);

	Mono<Boolean> put(K key, V value);

}
