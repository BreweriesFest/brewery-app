package com.brewery.app.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class Resilience4JConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(100)).build()).build());
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> mongoServiceCusomtizer() {
        return factory -> {
            factory.configure(builder -> builder
                    .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(20)).build())
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults()), "mongo");
            factory.addCircuitBreakerCustomizer(
                    circuitBreaker -> circuitBreaker.getEventPublisher()
                            .onError(event -> log.info("circuit-breaker error event {}", event.getEventType().name())),
                    "mongo");
        };
    }

    @Bean
    public Retry mongoServiceRetryCustomizer() {
        return Retry.ofDefaults("mongo");
    }
}
