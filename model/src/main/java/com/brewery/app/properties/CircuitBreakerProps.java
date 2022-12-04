package com.brewery.app.properties;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CircuitBreakerProps {
    @Value("${failureRateThreshold:${app.circuit-breaker.default.failureRateThreshold}}")
    private float failureRateThreshold;
    @Value("${permittedNumberOfCallsInHalfOpenState:${app.circuit-breaker.default.permittedNumberOfCallsInHalfOpenState}}")
    private int permittedNumberOfCallsInHalfOpenState;
    @Value("${slidingWindowSize:${app.circuit-breaker.default.slidingWindowSize}}")
    private int slidingWindowSize;
    @Value("${slidingWindowType:${app.circuit-breaker.default.slidingWindowType}}")
    private CircuitBreakerConfig.SlidingWindowType slidingWindowType;
    @Value("${minimumNumberOfCalls:${app.circuit-breaker.default.minimumNumberOfCalls}}")
    private int minimumNumberOfCalls;
    @Value("${slowCallRateThreshold:${app.circuit-breaker.default.slowCallRateThreshold}}")
    private float slowCallRateThreshold;
    @Value("${slowCallDurationThreshold:${app.circuit-breaker.default.slowCallDurationThreshold}}")
    private long slowCallDurationThreshold;
    @Value("${maxWaitDurationInHalfOpenState:${app.circuit-breaker.default.maxWaitDurationInHalfOpenState}}")
    private long maxWaitDurationInHalfOpenState;
}
