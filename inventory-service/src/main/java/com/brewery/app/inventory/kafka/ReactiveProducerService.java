package com.brewery.app.inventory.kafka;

import com.brewery.app.domain.InventoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReactiveProducerService {
    private final ReactiveKafkaProducerTemplate<String, InventoryDTO> reactiveKafkaProducerTemplate;

    // @Value(value = "${FAKE_PRODUCER_DTO_TOPIC}")
    private String topic = "test";

    public void send(InventoryDTO fakeProducerDTO) {
        log.info("send to topic={}, {}={},", topic, InventoryDTO.class.getSimpleName(), fakeProducerDTO);
        reactiveKafkaProducerTemplate.send(topic, fakeProducerDTO).doOnSuccess(senderResult -> log
                .info("sent {} offset : {}", fakeProducerDTO, senderResult.recordMetadata().offset())).subscribe();
    }
}
