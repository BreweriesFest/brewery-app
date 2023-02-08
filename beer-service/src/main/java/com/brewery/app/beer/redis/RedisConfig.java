package com.brewery.app.beer.redis;

import com.brewery.app.model.BeerDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	// @Bean
	// public ReactiveRedisConnectionFactory connectionFactory() {
	// return new LettuceConnectionFactory("localhost", 6379);
	// }

	@Bean
	public ReactiveRedisTemplate<String, BeerDto> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
		RedisSerializationContext<String, BeerDto> serializationContext = RedisSerializationContext
				.<String, BeerDto>newSerializationContext().key(StringRedisSerializer.UTF_8)
				.value(new Jackson2JsonRedisSerializer<>(BeerDto.class)).hashKey(StringRedisSerializer.UTF_8)
				.hashValue(new Jackson2JsonRedisSerializer<>(BeerDto.class)).build();
		return new ReactiveRedisTemplate<>(factory, serializationContext);
	}

}
