package com.brewery.common.cache;

import com.brewery.common.util.Helper;
import com.brewery.model.domain.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class CacheServiceImpl<K, V extends Record<K>> extends CacheConfig<K, V> implements CacheService<K, V> {

	public CacheServiceImpl(ReactiveRedisConnectionFactory factory, RedisSerializer<K> keySerializer,
			RedisSerializer<V> valueSerializer) {
		super(factory, keySerializer, valueSerializer);
	}

	public Mono<Map<K, V>> get(Collection<K> keys) {
		return reactiveRedisTemplate.opsForValue()
			.multiGet(keys)
			.timeout(timeout)
			.map(cache -> Helper.collectionAsStream(cache)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(V::key, Function.identity())))
			.onErrorResume(e -> {
				log.error("error in cache", e);
				return Mono.just(new HashMap<>());
			});
	}

	public Mono<V> get(K key) {
		return reactiveRedisTemplate.opsForValue().getAndExpire(key, expiration).timeout(timeout).onErrorResume(e -> {
			log.error("error in cache", e);
			return Mono.empty();
		});
	}

	public Mono<Boolean> put(Map<K, V> values) {
		return reactiveRedisTemplate.opsForValue().multiSet(values).timeout(timeout).onErrorReturn(false);
	}

	public Mono<Boolean> put(K key, V value) {
		return reactiveRedisTemplate.opsForValue().set(key, value, expiration).timeout(timeout).onErrorReturn(false);
	}

}
