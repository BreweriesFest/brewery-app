package com.brewery.common.kafka.consumer;

import com.brewery.common.util.AppConstant;
import com.brewery.common.util.Helper;
import com.brewery.model.domain.Record;
import com.brewery.model.properties.kafka.KafkaConsumerProps;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Getter
@Slf4j
public abstract class ReactiveConsumerConfig<K, V extends Record<K>> {

	protected final ReactiveKafkaConsumerTemplate<K, V> reactiveKafkaConsumerTemplate;

	protected final MicrometerConsumerListener<K, V> micrometerConsumerListener;

	protected final String topic;

	protected final AtomicReference<Consumer<K, V>> consumerToUnregister = new AtomicReference<>();

	protected final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

	protected ReactiveConsumerConfig(KafkaConsumerProps kafkaProperties, Class<?> serializer, Class<?> deSerializer,
			DeadLetterPublishingRecoverer deadLetterPublishingRecoverer, MeterRegistry meterRegistry) {
		this.topic = kafkaProperties.getTopic();
		var kafkaReceiverOptions = kafkaReceiverOptions(kafkaProperties, serializer, deSerializer);
		micrometerConsumerListener = new MicrometerConsumerListener<>(meterRegistry);
		reactiveKafkaConsumerTemplate = new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
		this.deadLetterPublishingRecoverer = deadLetterPublishingRecoverer;

	}

	protected ReactiveConsumerConfig(KafkaConsumerProps kafkaProperties, Class<?> serializer, Class<?> deSerializer,
			MeterRegistry meterRegistry) {
		this.topic = kafkaProperties.getTopic();
		var kafkaReceiverOptions = kafkaReceiverOptions(kafkaProperties, serializer, deSerializer);
		micrometerConsumerListener = new MicrometerConsumerListener<>(meterRegistry);
		reactiveKafkaConsumerTemplate = new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
		this.deadLetterPublishingRecoverer = null;

	}

	public Flux<ReceiverRecord<K, V>> consumerRecord(Function<ReceiverRecord<K, V>, Mono<?>> input) {
		return reactiveKafkaConsumerTemplate
			// .assignment().flatMap(a->reactiveKafkaConsumerTemplate.resume(a))

			.receive()
			.parallel(10)
			.runOn(Schedulers.parallel())
			// .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE

			.doOnNext(consumerRecord -> {
				log.info("received  topic={}, partition={}, offset={}",
						// consumerRecord.key(), consumerRecord.value(),
						// consumerRecord.headers().toArray(),
						consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
			})
			.map(__ -> {
				input.apply(__)
					.contextWrite(ctx -> ctx
						.putAllMap(Helper.extractHeaders(List.of(AppConstant.TENANT_ID, AppConstant.CUSTOMER_ID), __)))
					.map(mono -> __)
					.onErrorResume(error -> {
						log.error("exception in consuming {}", error.getMessage(), error);
						return Mono.error(new ReceiverRecordException(__, error));
					})
					.retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
						.transientErrors(true)
						.onRetryExhaustedThrow((a, b) -> b.failure()))
					.doOnError(e -> {
						log.error("publish to dlq", e);
						// ReceiverRecordException ex = (ReceiverRecordException) e;
						// if (Objects.nonNull(deadLetterPublishingRecoverer))
						// deadLetterPublishingRecoverer.accept(ex.getRecord(), ex);
					})
					.map(___ -> {
						log.info("acknowledge record");
						__.receiverOffset().acknowledge();
						return __;
					})
					.subscribe();
				return __;
			})
			.doOnSubscribe(subs -> {
				reactiveKafkaConsumerTemplate.doOnConsumer(consumer -> {
					micrometerConsumerListener.consumerAdded("consuming::" + topic, consumer);
					consumerToUnregister.set(consumer);
					return Mono.empty();
				}).subscribe();
			})
			.doOnError(e -> {
				Consumer<K, V> consumer = consumerToUnregister.getAndSet(null);
				if (consumer != null) {
					micrometerConsumerListener.consumerRemoved("consuming::" + topic, consumer);
				}
			})
			.sequential()
			.repeat();

	}

	private ReceiverOptions<K, V> kafkaReceiverOptions(KafkaConsumerProps kafkaProperties, Class<?> serializer,
			Class<?> deSerializer) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, serializer);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deSerializer);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumerGroup());
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		return ReceiverOptions.<K, V>create(props)
			.commitInterval(Duration.ZERO)
			.commitBatchSize(0)
			.addAssignListener(receiverPartitions -> log.info("assigned partition {}", receiverPartitions))
			.addRevokeListener(receiverPartitions -> log.info("revoke partition {}", receiverPartitions))
			.subscription(Collections.singletonList(kafkaProperties.getTopic()));
	}

}