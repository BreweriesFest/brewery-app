package com.brewery.app.inventory.kafka;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.kafka.consumer.ReactiveConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PreDestroy;
import java.util.function.Function;

import static com.brewery.app.util.AppConstant.TENANT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveConsumerService {
    Function<ReceiverRecord<String, InventoryDTO>, Mono<ReceiverRecord<String, InventoryDTO>>> processRecord = record -> Mono
            .deferContextual(ctx -> {
                String tenant = ctx.get(TENANT_ID);
                log.info("getting context:: {}", tenant);
                return Mono.just(record);
            }).map(receiverRecord1 -> {
                log.info("consumeing");
                return receiverRecord1;
            }

            );
    private Disposable.Composite disposables = Disposables.composite();

    @Bean
    ApplicationListener<ApplicationReadyEvent> factoryBeanListener(
            ReactiveConsumerConfig<String, InventoryDTO> reactiveConsumer) {
        return event -> disposables.add(reactiveConsumer.consumerRecord(processRecord).subscribe());
        // return event -> reactiveConsumer.consumerRecord(processRecord);
    }

    @PreDestroy
    void disconnect() {
        log.info("dispose");
        disposables.dispose();
    }
}
