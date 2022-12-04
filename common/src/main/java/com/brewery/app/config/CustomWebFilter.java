package com.brewery.app.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.Helper.getHeader;

@Component
public class CustomWebFilter implements WebFilter {

    public static final String PATH_GRAPHQL = "/graphql";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).contextWrite(__ -> {
            if (Objects.equals(exchange.getRequest().getURI().getPath(), PATH_GRAPHQL))
                return __.putAllMap(Map.of(TENANT_ID, getHeader.apply(exchange, TENANT_ID), CUSTOMER_ID,
                        getHeader.apply(exchange, CUSTOMER_ID)));
            return __;
        });
    }
}
