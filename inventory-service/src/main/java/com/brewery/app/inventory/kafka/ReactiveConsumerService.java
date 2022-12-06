package com.brewery.app.inventory.kafka;

import com.brewery.app.domain.InventoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;

import javax.annotation.PreDestroy;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveConsumerService {
    private Disposable.Composite disposables = Disposables.composite();

    @Bean
    ApplicationListener<ApplicationReadyEvent> factoryBeanListener(
            Flux<ConsumerRecord<String, InventoryDTO>> reactiveKafkaConsumer) {
        return event -> disposables.add(reactiveKafkaConsumer
                .subscribe((c) -> log.info("processing record::{}", c.value()), err -> log.error("err")));
    }

    @PreDestroy
    void disconnect() {
        log.info("dispose");
        disposables.dispose();
    }
}
