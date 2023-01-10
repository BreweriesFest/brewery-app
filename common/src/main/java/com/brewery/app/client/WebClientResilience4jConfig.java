package com.brewery.app.client;

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
import org.springframework.context.annotation.Profile;

import static com.brewery.app.util.AppConstant.RESILIENCE_ID_BEER_CLIENT;
import static com.brewery.app.util.AppConstant.RESILIENCE_ID_INVENTORY_CLIENT;

@Configuration
@RequiredArgsConstructor
public class WebClientResilience4jConfig {

	private final Resilience4JConfig resilience4JConfig;

	@Configuration
	@Profile("beer-client-service")
	public class BeerClientConfig {

		@Bean
		@ConfigurationProperties(prefix = "app.client.beer.circuit-breaker")
		public CircuitBreakerProps beerClientCircuitBreakerProps() {
			return new CircuitBreakerProps();
		}

		@Bean
		@ConfigurationProperties(prefix = "app.client.beer.time-limiter")
		public TimeLimiterProps beerClientTimeLimiterProps() {
			return new TimeLimiterProps();
		}

		@Bean
		@ConfigurationProperties(prefix = "app.client.beer.retry")
		public RetryProps beerClientRetryProps() {
			return new RetryProps();
		}

		@Bean
		public Customizer<ReactiveResilience4JCircuitBreakerFactory> beerClientCustomizer(
				CircuitBreakerProps beerClientCircuitBreakerProps, TimeLimiterProps beerClientTimeLimiterProps) {
			return resilience4JConfig.configureCircuitBreakerCustomizer(RESILIENCE_ID_BEER_CLIENT,
					beerClientCircuitBreakerProps, beerClientTimeLimiterProps);
		}

		@Bean
		public Retry beerClientRetry(RetryProps beerClientRetryProps, MeterRegistry meterRegistry) {
			return resilience4JConfig.configureRetryCustomizer(RESILIENCE_ID_BEER_CLIENT, beerClientRetryProps,
					meterRegistry);
		}

	}

	@Configuration
	@Profile("inventory-client-service")
	public class InventoryClientConfig {

		@Bean
		@ConfigurationProperties(prefix = "app.client.inventory.circuit-breaker")
		public CircuitBreakerProps inventoryClientCircuitBreakerProps() {
			return new CircuitBreakerProps();
		}

		@Bean
		@ConfigurationProperties(prefix = "app.client.inventory.time-limiter")
		public TimeLimiterProps inventoryClientTimeLimiterProps() {
			return new TimeLimiterProps();
		}

		@Bean
		@ConfigurationProperties(prefix = "app.client.inventory.retry")
		public RetryProps inventoryClientRetryProps() {
			return new RetryProps();
		}

		@Bean
		public Customizer<ReactiveResilience4JCircuitBreakerFactory> inventoryClientCustomizer(
				CircuitBreakerProps inventoryClientCircuitBreakerProps,
				TimeLimiterProps inventoryClientTimeLimiterProps) {
			return resilience4JConfig.configureCircuitBreakerCustomizer(RESILIENCE_ID_INVENTORY_CLIENT,
					inventoryClientCircuitBreakerProps, inventoryClientTimeLimiterProps);
		}

		@Bean
		public Retry inventoryClientRetry(RetryProps inventoryClientRetryProps, MeterRegistry meterRegistry) {
			return resilience4JConfig.configureRetryCustomizer(RESILIENCE_ID_INVENTORY_CLIENT,
					inventoryClientRetryProps, meterRegistry);
		}

	}

}
