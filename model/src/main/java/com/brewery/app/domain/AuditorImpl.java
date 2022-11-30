package com.brewery.app.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuditorImpl implements ReactiveAuditorAware<String> {

    @Override
    public Mono<String> getCurrentAuditor() {
        log.info("inside auditor {}", Thread.currentThread().getName());
        return Mono.deferContextual(ctx -> Mono.just(ctx.get("customerId")));
    }
}