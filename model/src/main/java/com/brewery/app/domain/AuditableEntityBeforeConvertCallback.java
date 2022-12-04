package com.brewery.app.domain;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuditableEntityBeforeConvertCallback implements ReactiveBeforeConvertCallback<Auditable> {

    @Override
    public Publisher<Auditable> onBeforeConvert(Auditable entity, String collection) {
        log.info("inside beforeConvertCallback {}", Thread.currentThread().getName());
        return Mono.deferContextual(ctx -> {
            entity.setTenantId(ctx.get("tenantId"));
            return Mono.just(entity);
        });
    }
}
