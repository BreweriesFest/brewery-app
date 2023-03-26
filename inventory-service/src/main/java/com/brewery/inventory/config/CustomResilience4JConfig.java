package com.brewery.inventory.config;

import com.brewery.common.config.Resilience4JConfig;
import com.brewery.model.properties.CircuitBreakerProps;
import com.brewery.model.properties.RetryProps;
import com.brewery.model.properties.TimeLimiterProps;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.brewery.common.util.AppConstant.RESILIENCE_ID_MONGO;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class CustomResilience4JConfig {

	private final Resilience4JConfig resilience4JConfig;

	@Bean
	@ConfigurationProperties(prefix = "app.mongo-service.circuit-breaker")
	public CircuitBreakerProps monoCircuitBreakerProps() {
		return new CircuitBreakerProps();
	}

	@Bean
	@ConfigurationProperties(prefix = "app.mongo-service.time-limiter")
	public TimeLimiterProps mongoTimeLimiterProps() {
		return new TimeLimiterProps();
	}

	@Bean
	@ConfigurationProperties(prefix = "app.mongo-service.retry")
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
	public Retry mongoServiceRetry(RetryProps mongoRetryProps, MeterRegistry meterRegistry) {
		return resilience4JConfig.configureRetryCustomizer(RESILIENCE_ID_MONGO, mongoRetryProps, meterRegistry);
	}

}
