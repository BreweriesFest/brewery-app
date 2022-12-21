package com.brewery.app.config;

import com.brewery.app.properties.CircuitBreakerProps;
import com.brewery.app.properties.RetryProps;
import com.brewery.app.properties.TimeLimiterProps;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    @ConfigurationProperties(prefix = "app.circuit-breaker.default")
    public CircuitBreakerProps defaultCircuitBreakerProps() {
        return new CircuitBreakerProps();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.time-limiter.default")
    public TimeLimiterProps defaultTimeLimiterProps() {
        return new TimeLimiterProps();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(
            CircuitBreakerProps defaultCircuitBreakerProps, TimeLimiterProps defaultTimeLimiterProps) {
        return factory -> factory.configureDefault(
                id -> new Resilience4JConfigBuilder(id).circuitBreakerConfig(configure(defaultCircuitBreakerProps))
                        .timeLimiterConfig(configure(defaultTimeLimiterProps)).build());
    }

    public Customizer<ReactiveResilience4JCircuitBreakerFactory> configureCircuitBreakerCustomizer(String id,
            CircuitBreakerProps monoCircuitBreakerProps, TimeLimiterProps mongoTimeLimiterProps) {
        return factory -> factory.configure(builder -> builder.timeLimiterConfig(configure(mongoTimeLimiterProps))
                .circuitBreakerConfig(configure(monoCircuitBreakerProps)), id);
    }

    private CircuitBreakerConfig configure(CircuitBreakerProps circuitBreakerProps) {
        return CircuitBreakerConfig.custom().failureRateThreshold(circuitBreakerProps.getFailureRateThreshold())
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProps.getPermittedNumberOfCallsInHalfOpenState())
                .slidingWindow(circuitBreakerProps.getSlidingWindowSize(),
                        circuitBreakerProps.getMinimumNumberOfCalls(), circuitBreakerProps.getSlidingWindowType())
                .slowCallDurationThreshold(Duration.ofMillis(circuitBreakerProps.getSlowCallDurationThreshold()))
                .slowCallRateThreshold(circuitBreakerProps.getSlowCallRateThreshold()).maxWaitDurationInHalfOpenState(
                        Duration.ofMillis(circuitBreakerProps.getMaxWaitDurationInHalfOpenState()))
                .build();
    }

    private TimeLimiterConfig configure(TimeLimiterProps timeLimiterProps) {
        return TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(timeLimiterProps.getTimeoutDuration()))
                .build();
    }

    public Retry configureRetryCustomizer(String id, RetryProps retryProps, MeterRegistry meterRegistry) {
        var registry = RetryRegistry.of(configure(retryProps, meterRegistry));
        TaggedRetryMetrics.ofRetryRegistry(registry).bindTo(meterRegistry);
        return registry.retry(id);
    }

    private RetryConfig configure(RetryProps retryProps, MeterRegistry meterRegistry) {
        return RetryConfig.custom().maxAttempts(retryProps.getMaxAttempts()).build();
    }
}
