package com.brewery.app.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.Helper.fetchHeaderFromContext;

@Component
@Slf4j
public class AuditorImpl implements ReactiveAuditorAware<String> {

    @Override
    public Mono<String> getCurrentAuditor() {
        return Mono.deferContextual(ctx -> Mono.just(fetchHeaderFromContext.apply(CUSTOMER_ID, ctx)));
    }
}