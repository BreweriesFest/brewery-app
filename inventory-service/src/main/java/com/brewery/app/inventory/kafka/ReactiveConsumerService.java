package com.brewery.app.inventory.kafka;

import com.brewery.app.domain.InventoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveConsumerService {
    private final ReactiveKafkaConsumerTemplate<String, InventoryDTO> reactiveKafkaConsumerTemplate;
    private Disposable.Composite disposables = Disposables.composite();

    @Bean
    ApplicationListener<ApplicationReadyEvent> factoryBeanListener() {
        return event -> disposables
                .add(consumeFakeConsumerDTO().subscribe((c) -> log.info("err", c.beerId()), err -> log.error("err")));
    }

    private Flux<InventoryDTO> consumeFakeConsumerDTO() {
        return reactiveKafkaConsumerTemplate
                // .assignment().flatMap(a->reactiveKafkaConsumerTemplate.resume(a)).subscribe()
                .receiveAutoAck().publishOn(Schedulers.boundedElastic())
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.offset()))
                .map(ConsumerRecord::value)
                .doOnNext(fakeConsumerDTO -> log.info("successfully consumed {}={}", InventoryDTO.class.getSimpleName(),
                        fakeConsumerDTO))
                .doOnError(
                        throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()));
    }

    @PreDestroy
    void disconnect() {
        log.info("dispose");
        disposables.dispose();
    }
}
