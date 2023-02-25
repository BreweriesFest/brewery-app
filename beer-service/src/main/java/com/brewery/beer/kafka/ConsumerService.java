package com.brewery.beer.kafka;

import com.brewery.beer.service.BeerService;
import com.brewery.common.kafka.consumer.ReactiveConsumerConfig;
import com.brewery.model.event.CheckInventoryEvent;
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

	private Disposable.Composite disposables = Disposables.composite();

	private BeerService beerService;

	private Function<ReceiverRecord<String, CheckInventoryEvent>, Mono<?>> processRecord = record -> {
		return this.beerService.consumeCheckInventoryEvent(record.value());
	};

	public ConsumerService(BeerService beerService) {
		this.beerService = beerService;
	}

	@Bean
	public ApplicationListener<ApplicationReadyEvent> factoryBeanListener(
			ReactiveConsumerConfig<String, CheckInventoryEvent> reactiveConsumer) {
		return event -> disposables.add(reactiveConsumer.consumerRecord(processRecord).subscribe());
	}

	@PreDestroy
	void disconnect() {
		log.info("dispose");
		disposables.dispose();
	}

}
