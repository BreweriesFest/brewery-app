package com.brewery.common.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Value("${app.default.timezone}")
	private String timezone;

	@PostConstruct
	public void init() {
		// Setting Spring Boot SetTimeZone
		Hooks.enableAutomaticContextPropagation();
		TimeZone.setDefault(TimeZone.getTimeZone(timezone));
	}

	@Bean
	public HttpClient httpClient() {
		return HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
			.responseTimeout(Duration.ofMillis(20000))
			.compress(true)
			.doOnConnected(__ -> __.addHandlerLast(new ReadTimeoutHandler(20000, TimeUnit.MILLISECONDS))
				.addHandlerLast(new WriteTimeoutHandler(20000, TimeUnit.MILLISECONDS)));
	}

	@Bean
	public WebClient webClient(WebClient.Builder webClientBuilder, HttpClient httpClient) {
		return webClientBuilder.codecs(config -> config.defaultCodecs().maxInMemorySize(262144))
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}

	@Bean
	public HttpGraphQlClient httpGraphQlClient(WebClient webClient) {
		return HttpGraphQlClient.builder(webClient).build();
	}

	@Bean
	public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		observationRegistry.observationConfig().observationHandler(new SimpleLoggingHandler());

		return new ObservedAspect(observationRegistry);
	}

}
