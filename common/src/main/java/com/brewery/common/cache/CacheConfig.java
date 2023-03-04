package com.brewery.common.cache;

import com.brewery.model.domain.Record;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Slf4j
@Getter
public abstract class CacheConfig<K, V extends Record<K>> {

	protected final ReactiveRedisTemplate<K, V> reactiveRedisTemplate;

	protected final Duration timeout;

	protected final Duration expiration;

	public CacheConfig(ReactiveRedisConnectionFactory factory, RedisSerializer<K> keySerializer,
			RedisSerializer<V> valueSerializer) {
		RedisSerializationContext<K, V> serializationContext = RedisSerializationContext.<K, V>newSerializationContext()
			.key(keySerializer)
			.value(valueSerializer)
			.hashKey(keySerializer)
			.hashValue(valueSerializer)
			.build();
		this.reactiveRedisTemplate = new ReactiveRedisTemplate<>(factory, serializationContext);
		this.timeout = Duration.ofMillis(50);
		this.expiration = Duration.ofSeconds(50);
	}

}
