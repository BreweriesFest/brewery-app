package com.brewery.model.properties;

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

	@Value("${failureRateThreshold:${app.default.circuit-breaker.failureRateThreshold}}")
	private float failureRateThreshold;

	@Value("${permittedNumberOfCallsInHalfOpenState:${app.default.circuit-breaker.permittedNumberOfCallsInHalfOpenState}}")
	private int permittedNumberOfCallsInHalfOpenState;

	@Value("${slidingWindowSize:${app.default.circuit-breaker.slidingWindowSize}}")
	private int slidingWindowSize;

	@Value("${slidingWindowType:${app.default.circuit-breaker.slidingWindowType}}")
	private CircuitBreakerConfig.SlidingWindowType slidingWindowType;

	@Value("${minimumNumberOfCalls:${app.default.circuit-breaker.minimumNumberOfCalls}}")
	private int minimumNumberOfCalls;

	@Value("${slowCallRateThreshold:${app.default.circuit-breaker.slowCallRateThreshold}}")
	private float slowCallRateThreshold;

	@Value("${slowCallDurationThreshold:${app.default.circuit-breaker.slowCallDurationThreshold}}")
	private long slowCallDurationThreshold;

	@Value("${maxWaitDurationInHalfOpenState:${app.default.circuit-breaker.maxWaitDurationInHalfOpenState}}")
	private long maxWaitDurationInHalfOpenState;

}
