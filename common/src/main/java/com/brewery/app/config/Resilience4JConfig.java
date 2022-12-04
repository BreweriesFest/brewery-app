package com.brewery.app.config;

import com.brewery.app.properties.CircuitBreakerProps;
import com.brewery.app.properties.RetryProps;
import com.brewery.app.properties.TimeLimiterProps;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
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

    public static final String RESILIENCE_ID_MONGO = "mongo";

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
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(
            CircuitBreakerProps defaultCircuitBreakerProps, TimeLimiterProps defaultTimeLimiterProps) {
        return factory -> factory.configureDefault(
                id -> new Resilience4JConfigBuilder(id).circuitBreakerConfig(configure(defaultCircuitBreakerProps))
                        .timeLimiterConfig(configure(defaultTimeLimiterProps)).build());
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> mongoServiceCustomizer(
            CircuitBreakerProps monoCircuitBreakerProps, TimeLimiterProps mongoTimeLimiterProps) {
        return factory -> factory.configure(builder -> builder.timeLimiterConfig(configure(mongoTimeLimiterProps))
                .circuitBreakerConfig(configure(monoCircuitBreakerProps)), RESILIENCE_ID_MONGO);
    }

    public CircuitBreakerConfig configure(CircuitBreakerProps circuitBreakerProps) {
        return CircuitBreakerConfig.custom().failureRateThreshold(circuitBreakerProps.getFailureRateThreshold())
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProps.getPermittedNumberOfCallsInHalfOpenState())
                .slidingWindow(circuitBreakerProps.getSlidingWindowSize(),
                        circuitBreakerProps.getMinimumNumberOfCalls(), circuitBreakerProps.getSlidingWindowType())
                .slowCallDurationThreshold(Duration.ofMillis(circuitBreakerProps.getSlowCallDurationThreshold()))
                .slowCallRateThreshold(circuitBreakerProps.getSlowCallRateThreshold()).maxWaitDurationInHalfOpenState(
                        Duration.ofMillis(circuitBreakerProps.getMaxWaitDurationInHalfOpenState()))
                .build();
    }

    public TimeLimiterConfig configure(TimeLimiterProps timeLimiterProps) {
        return TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(timeLimiterProps.getTimeoutDuration()))
                .build();
    }

    @Bean
    public Retry mongoServiceRetryCustomizer(RetryProps mongoRetryProps) {
        return Retry.of(RESILIENCE_ID_MONGO, configure(mongoRetryProps));
    }

    public RetryConfig configure(RetryProps retryProps) {
        return RetryConfig.custom().maxAttempts(retryProps.getMaxAttempts()).build();
    }
}
