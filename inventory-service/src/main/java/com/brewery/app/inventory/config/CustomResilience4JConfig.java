package com.brewery.app.inventory.config;

import com.brewery.app.config.Resilience4JConfig;
import com.brewery.app.properties.CircuitBreakerProps;
import com.brewery.app.properties.RetryProps;
import com.brewery.app.properties.TimeLimiterProps;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.brewery.app.util.AppConstant.RESILIENCE_ID_MONGO;

@Configuration
@RequiredArgsConstructor
public class CustomResilience4JConfig {

    private final Resilience4JConfig resilience4JConfig;

    @Bean
    @ConfigurationProperties(prefix = "app.circuit-breaker.mongo-service")
    public CircuitBreakerProps monoCircuitBreakerProps() {
        return new CircuitBreakerProps();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.time-limiter.mongo-service")
    public TimeLimiterProps mongoTimeLimiterProps() {
        return new TimeLimiterProps();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.retry.mongo-service")
    public RetryProps mongoRetryProps() {
        return new RetryProps();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> mongoServiceCustomizer(
            CircuitBreakerProps monoCircuitBreakerProps, TimeLimiterProps mongoTimeLimiterProps) {
        return resilience4JConfig.configureCircuitBreakerCustomizer(RESILIENCE_ID_MONGO, monoCircuitBreakerProps,
                mongoTimeLimiterProps);
    }

    @Bean
    public Retry configureRetryCustomizer(RetryProps mongoRetryProps, MeterRegistry meterRegistry) {
        return resilience4JConfig.configureRetryCustomizer(RESILIENCE_ID_MONGO, mongoRetryProps, meterRegistry);
    }

}
