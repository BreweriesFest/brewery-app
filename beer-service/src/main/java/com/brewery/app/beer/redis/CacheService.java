package com.brewery.app.beer.redis;

import com.brewery.app.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brewery.app.util.Helper.collectionAsStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

	private final ReactiveRedisTemplate<String, BeerDto> reactiveRedisTemplate;

	public Mono<Map<String, BeerDto>> getMultipleKeysWithTimeout(Collection<String> keys, Duration timeout) {
		return reactiveRedisTemplate.opsForValue().multiGet(keys).timeout(timeout)
				.map(cache -> collectionAsStream(cache).filter(Objects::nonNull)
						.collect(Collectors.toMap(BeerDto::id, Function.identity())))
				.onErrorResume(e -> Mono.just(new HashMap<>()));
	}

	public Mono<Void> setMultipleValues(Map<String, BeerDto> values) {
		List<Mono<Boolean>> setOperations = values.entrySet().stream()
				.map(entry -> reactiveRedisTemplate.opsForValue().set(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());

		return Flux.concat(setOperations).then();
	}

	public Mono<Boolean> setValue(String key, BeerDto value, Duration expiration) {
		return reactiveRedisTemplate.opsForValue().set(key, value, expiration).onErrorReturn(false);
	}

}
