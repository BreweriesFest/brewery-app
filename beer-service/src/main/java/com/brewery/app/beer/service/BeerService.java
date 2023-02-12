package com.brewery.app.beer.service;

import com.brewery.app.beer.mapper.BeerMapper;
import com.brewery.app.beer.redis.CacheService;
import com.brewery.app.beer.repository.Beer;
import com.brewery.app.beer.repository.BeerRepository;
import com.brewery.app.beer.repository.QBeer;
import com.brewery.app.client.InventoryClient;
import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.event.CheckInventoryEvent;
import com.brewery.app.exception.BusinessException;
import com.brewery.app.exception.ExceptionReason;
import com.brewery.app.kafka.producer.ReactiveProducerService;
import com.brewery.app.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brewery.app.exception.ExceptionReason.BEER_NOT_FOUND;
import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.Helper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerService {

	private final BeerRepository beerRepository;

	private final BeerMapper beerMapper;

	private final ReactiveProducerService<String, BrewBeerEvent> reactiveProducerService;

	private final InventoryClient inventoryClient;

	private final CacheService cacheService;

	@Value("${features.cache.enabled}")
	private boolean redisEnabled;

	public Flux<BeerDto> findBeerById(Collection<String> beerId) {
		Set<String> uniqueBeerId = new HashSet<>(beerId);

		Mono<Map<String, BeerDto>> redisValues = redisEnabled
				? cacheService.getMultipleKeysWithTimeout(uniqueBeerId, Duration.ofSeconds(20))
				: Mono.just(new HashMap<>());

		return redisValues.flatMapMany(values -> {
			List<String> missingKeys = uniqueBeerId.stream().filter(key -> !values.containsKey(key))
					.collect(Collectors.toList());

			if (missingKeys.isEmpty()) {
				return Flux.fromIterable(values.values());
			}
			else {
				return fetchMissingValues(missingKeys).doOnNext(dbValue -> values.put(dbValue.id(), dbValue))
						.thenMany(Flux.fromIterable(values.values()));
			}
		});
	}

	private Flux<BeerDto> fetchMissingValues(List<String> missingKeys) {
		return Flux.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findAll((qBeer.id.in(missingKeys))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).map(beerMapper::fromBeer)
				.flatMap(dbValue -> redisEnabled
						? cacheService.setValue(dbValue.id(), dbValue, Duration.ofSeconds(200)).thenReturn(dbValue)
						: Mono.just(dbValue), 10);
	}

	public Mono<BeerDto> saveBeer(BeerDto beerDto) {

		return Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository
					.exists((qBeer.name.equalsIgnoreCase(beerDto.name()).or(qBeer.upc.equalsIgnoreCase(beerDto.upc())))
							.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx)))
							.and(qBeer.active.eq(true)));
		}).flatMap(exists -> exists ? Mono.error(new BusinessException(ExceptionReason.BEER_ALREADY_PRESENT))
				: Mono.just(beerDto)).map(beerMapper::fromBeerDto).flatMap(beerRepository::save)
				.map(beerMapper::fromBeer).doOnError(exc -> log.error("exception {}", exc));

	}

	public Mono<BeerDto> updateBeer(String beerId, BeerDto beerDto) {

		return Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findOne((qBeer.id.eq(beerId))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).switchIfEmpty(Mono.error(new BusinessException(BEER_NOT_FOUND)))
				.map(beer -> beerMapper.fromBeerDto(beerDto, beer)).flatMap(beerRepository::save)
				.map(beerMapper::fromBeer);
	}

	public Mono<String> deleteById(String beerId) {
		return Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.exists((qBeer.id.eq(beerId))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).flatMap(__ -> {
			if (__.booleanValue())
				return beerRepository.deleteById(beerId).map(___ -> beerId);
			return Mono.error(new BusinessException(BEER_NOT_FOUND));
		});
	}

	public Mono<BeerDto> findBeerByUpc(String upc) {
		return Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findOne((qBeer.upc.equalsIgnoreCase(upc))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).map(beerMapper::fromBeer);
	}

	public Flux<BeerDto> findAllBeer() {
		return Flux.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findAll(
					qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx)).and(qBeer.active.eq(true)));
		}).map(beerMapper::fromBeer);
	}

	public Mono<SenderResult<Void>> consumeCheckInventoryEvent(CheckInventoryEvent event) {
		return Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findOne((qBeer.id.eq(event.beerId()))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).flatMap(this::checkBeerInventory);

	}

	public Mono<SenderResult<Void>> checkBeerInventory(Beer beer) {
		return inventoryClient.getInventoryByBeerId(List.of(beer.getId()))
				.single(new InventoryDTO(null, beer.getId(), 0)).filter(__ -> beer.getMinQty() > __.qtyOnHand())
				.map(___ -> new BrewBeerEvent(uuid.get(), ___.beerId(), beer.getMinQty() - ___.qtyOnHand()))
				.flatMap(__ -> reactiveProducerService.send(__, Map.of()));

	}

	public Mono<Map<BeerDto, InventoryDTO>> inventory(List<BeerDto> beerDtos) {
		Map<String, BeerDto> beerDtoMap = collectionAsStream(beerDtos)
				.collect(Collectors.toMap(BeerDto::id, Function.identity()));
		return inventoryClient
				.getInventoryByBeerId(collectionAsStream(beerDtos).map(BeerDto::id).collect(Collectors.toList()))
				.groupBy(InventoryDTO::beerId)
				.flatMap(group -> group.next()
						.map(inventory -> Map.entry(beerDtoMap.get(inventory.beerId()), inventory)))
				.collectMap(Map.Entry::getKey, Map.Entry::getValue);
	}

}
