package com.brewery.beer.config;

import com.brewery.common.cache.CacheService;
import com.brewery.common.cache.CacheServiceImpl;
import com.brewery.model.dto.BeerDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheBean {

	@Bean
	public CacheService<String, BeerDto> reactiveCacheService(ReactiveRedisConnectionFactory factory) {
		return new CacheServiceImpl<>(factory, StringRedisSerializer.UTF_8,
				new Jackson2JsonRedisSerializer<>(BeerDto.class)) {
		};
	}

}
