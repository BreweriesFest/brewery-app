package com.brewery.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MyMeterRegistryConfiguration {

	// add custom global tags for all metrics
	@Bean
	public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return (registry) -> registry.config().commonTags("region", "us-east-1");
	}

}
