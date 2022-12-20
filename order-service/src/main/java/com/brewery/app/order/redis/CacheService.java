package com.brewery.app.order.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Bean
    ApplicationListener<ApplicationReadyEvent> factoryBeanListener() {
        return event -> {
            save();
            get();
        };
    }

    public void save() {
        reactiveRedisTemplate.opsForSet().add("test", "value").subscribe();
    }

    public void get() {
        reactiveRedisTemplate.opsForSet().pop("test").subscribe(System.out::println);
    }

}
