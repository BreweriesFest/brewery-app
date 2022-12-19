package com.brewery.app.audit;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.Helper.fetchHeaderFromContext;

@Component
@Slf4j
public class AuditableEntityBeforeConvertCallback implements ReactiveBeforeConvertCallback<Auditable> {

    @Override
    public Publisher<Auditable> onBeforeConvert(Auditable entity, String collection) {
        return Mono.deferContextual(ctx -> {
            entity.setTenantId(fetchHeaderFromContext.apply(TENANT_ID, ctx));
            return Mono.just(entity);
        });
    }
}
