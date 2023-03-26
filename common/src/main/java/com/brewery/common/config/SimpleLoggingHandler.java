package com.brewery.common.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class SimpleLoggingHandler implements ObservationHandler<Observation.Context> {

	@Override
	public boolean supportsContext(Observation.Context context) {
		return true;
	}

	@Override
	public void onStart(Observation.Context context) {
		log.info("Starting " + context.getName());
		context.put("time", System.currentTimeMillis());
	}

	@Override
	public void onScopeOpened(Observation.Context context) {
		log.info("Scope opened  " + context.getName());
	}

	@Override
	public void onScopeClosed(Observation.Context context) {
		log.info("Scope closed " + context.getName());
	}

	@Override
	public void onStop(Observation.Context context) {
		log.info("Stopping " + context.getName() + " duration "
				+ (System.currentTimeMillis() - context.getOrDefault("time", 0L)));
	}

	@Override
	public void onError(Observation.Context context) {
		log.error("Error " + Objects.requireNonNull(context.getError()).getMessage());
	}

}
