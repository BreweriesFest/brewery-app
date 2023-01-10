package com.brewery.app.inventory.kafka;

import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.event.OrderEvent;
import com.brewery.app.inventory.service.InventoryService;
import com.brewery.app.kafka.consumer.ReactiveConsumerConfig;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.function.Function;

@Service
@Slf4j
public class ConsumerService {

	private static InventoryService INVENTORY_SERVICE;

	private Function<ReceiverRecord<String, BrewBeerEvent>, Mono<?>> processBrewBeerEvent = record -> INVENTORY_SERVICE
			.addInventory(record.value());

	private Function<ReceiverRecord<String, OrderEvent>, Mono<?>> processAllocateBeerEvent = record -> INVENTORY_SERVICE
			.allocateInventory(record.value());

	private Disposable.Composite disposables = Disposables.composite();

	public ConsumerService(InventoryService inventoryService) {
		INVENTORY_SERVICE = inventoryService;
	}

	@Bean
	public ApplicationListener<ApplicationReadyEvent> brewBeerConsumer(
			ReactiveConsumerConfig<String, BrewBeerEvent> reactiveConsumer) {
		return event -> disposables.add(reactiveConsumer.consumerRecord(processBrewBeerEvent).subscribe());
	}

	@Bean
	public ApplicationListener<ApplicationReadyEvent> allocateBeerConsumer(
			ReactiveConsumerConfig<String, OrderEvent> reactiveConsumer) {
		return event -> disposables.add(reactiveConsumer.consumerRecord(processAllocateBeerEvent).subscribe());
	}

	@PreDestroy
	void disconnect() {
		log.info("dispose");
		disposables.dispose();
	}

}
