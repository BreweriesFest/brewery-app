package com.brewery.common.client;

import com.brewery.common.exception.BusinessException;
import com.brewery.common.exception.ExceptionReason;
import com.brewery.common.util.Helper;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

@Slf4j
public abstract class GraphqlClient {

	private final HttpGraphQlClient httpGraphQlClient;

	private final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

	private final Retry retry;

	private final String resilienceId;

	private final String url;

	protected GraphqlClient(HttpGraphQlClient httpGraphQlClient, String url,
			ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory, Retry retry, String resilienceId) {
		this.httpGraphQlClient = httpGraphQlClient;
		this.url = url;
		this.reactiveCircuitBreakerFactory = reactiveCircuitBreakerFactory;
		this.retry = retry;
		this.resilienceId = resilienceId;
	}

	protected Mono<HttpGraphQlClient> getHttpGraphQlClient(Collection<String> headers) {
		return Mono.deferContextual(ctx -> Mono.just(httpGraphQlClient.mutate().url(url).headers(httpHeaders -> {
			headers.forEach(__ -> httpHeaders.add(__, Helper.fetchHeaderFromContext.apply(__, ctx)));
		}).build()));
	}

	protected <T> Mono<T> fromMono(Collection<String> headers, String document, Map<String, Object> variables,
			ParameterizedTypeReference<T> entityType) {
		return getHttpGraphQlClient(headers)
			.flatMap(__ -> __.document(document).variables(variables).retrieve("data").toEntity(entityType))
			.transform(it -> {
				ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create(resilienceId);
				return rcb.run(it, throwable -> {
					log.error("exception::{}", throwable.getMessage(), throwable);
					return Mono.error(new BusinessException(ExceptionReason.UPSTREAM_SERVER_ERROR));
				});
			})
			.transformDeferred(RetryOperator.of(retry));
	}

	protected <T> Flux<T> fromFlux(Collection<String> headers, String document, Map<String, Object> variables,
			ParameterizedTypeReference<T> entityType) {
		return getHttpGraphQlClient(headers)
			.flatMap(__ -> __.document(document).variables(variables).retrieve("data").toEntityList(entityType))
			.flatMapMany(Flux::fromIterable)
			.transform(it -> {
				ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create(resilienceId);
				return rcb.run(it, throwable -> {
					log.error("exception::{}", throwable.getMessage(), throwable);
					return Flux.error(new BusinessException(ExceptionReason.UPSTREAM_SERVER_ERROR));
				});
			})
			.transformDeferred(RetryOperator.of(retry));
	}

}
