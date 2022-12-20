package com.brewery.app.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.brewery.app.util.Helper.fetchHeaderFromContext;

public abstract class GraphqlClient {

    private final HttpGraphQlClient httpGraphQlClient;
    private final String url;

    protected GraphqlClient(HttpGraphQlClient httpGraphQlClient, String url) {
        this.httpGraphQlClient = httpGraphQlClient;
        this.url = url;
    }

    protected Mono<HttpGraphQlClient> getHttpGraphQlClient(Collection<String> headers) {
        return Mono.deferContextual(ctx -> Mono.just(httpGraphQlClient.mutate().url(url).headers(httpHeaders -> {
            headers.forEach(__ -> httpHeaders.add(__, fetchHeaderFromContext.apply(__, ctx)));
        }).build()));
    }

    protected Mono<HttpGraphQlClient> getHttpGraphQlClient() {
        return getHttpGraphQlClient(List.of());
    }

    protected <T> Mono<T> getHttpGraphQlResponse(Collection<String> headers, String document,
            ParameterizedTypeReference<T> entityType) {
        return getHttpGraphQlResponse(headers, document, Map.of(), entityType);
    }

    protected <T> Mono<T> getHttpGraphQlResponse(Collection<String> headers, String document,
            Map<String, Object> variables, ParameterizedTypeReference<T> entityType) {
        return getHttpGraphQlClient(headers)
                .flatMap(__ -> __.document(document).variables(variables).retrieve("data").toEntity(entityType));
    }
}
