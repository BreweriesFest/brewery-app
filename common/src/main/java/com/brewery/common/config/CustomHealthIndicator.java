package com.brewery.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomHealthIndicator implements ReactiveHealthIndicator {

	private final ApplicationContext context;

	@Override
	public Mono<Health> health() {
		return doHealthCheck().onErrorResume((exception) -> Mono.just(new Health.Builder().down(exception).build()))
			.doOnNext(healthStatus -> {
				if (healthStatus.getStatus().equals(Status.DOWN))
					AvailabilityChangeEvent.publish(this.context, ReadinessState.REFUSING_TRAFFIC);
			});
	}

	private Mono<Health> doHealthCheck() {
		// perform some specific health check
		return Mono.just(Health.up().build());
	}

}
