package com.brewery.app.config;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;

@Component
public class HeaderInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        return chain.next(request).contextWrite(__ -> __.putAllMap(Map.of(TENANT_ID,
                request.getHeaders().getFirst(TENANT_ID), CUSTOMER_ID, request.getHeaders().getFirst(CUSTOMER_ID))));
    }
}