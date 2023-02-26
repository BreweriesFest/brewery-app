package com.brewery.beer.service;

import com.brewery.beer.mapper.BeerMapper;
import com.brewery.beer.repository.Beer;
import com.brewery.beer.repository.BeerRepository;
import com.brewery.beer.repository.QBeer;
import com.brewery.common.cache.CacheService;
import com.brewery.common.client.InventoryClient;
import com.brewery.common.exception.BusinessException;
import com.brewery.common.exception.ExceptionReason;
import com.brewery.common.kafka.producer.ReactiveProducerService;
import com.brewery.model.domain.InventoryDTO;
import com.brewery.model.dto.BeerDto;
import com.brewery.model.event.BrewBeerEvent;
import com.brewery.model.event.CheckInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brewery.common.exception.ExceptionReason.BEER_NOT_FOUND;
import static com.brewery.common.util.AppConstant.TENANT_ID;
import static com.brewery.common.util.Helper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerService {

	private final BeerRepository beerRepository;

	private final BeerMapper beerMapper;

	private final ReactiveProducerService<String, BrewBeerEvent> reactiveProducerService;

	private final InventoryClient inventoryClient;

	@Nullable
	private final CacheService<String, BeerDto> reactiveCacheService;

	@Value("${features.cache.enabled}")
	private boolean redisEnabled;

	public Flux<BeerDto> findBeerById(Collection<String> beerId) {
		var validateHeaders = validateContext();
		Set<String> uniqueBeerId = new HashSet<>(beerId);

		Mono<Map<String, BeerDto>> redisValues = redisEnabled ? reactiveCacheService.get(uniqueBeerId)
				: Mono.just(new HashMap<>());

		return validateHeaders.thenMany(redisValues.flatMapMany(values -> {
			List<String> missingKeys = uniqueBeerId.stream().filter(key -> !values.containsKey(key))
					.collect(Collectors.toList());

			if (missingKeys.isEmpty()) {
				return Flux.fromIterable(values.values());
			}
			else {
				return fetchMissingValues(missingKeys).doOnNext(dbValue -> values.put(dbValue.id(), dbValue))
						.thenMany(Flux.fromIterable(values.values()));
			}
		}));
	}

	private Flux<BeerDto> fetchMissingValues(List<String> missingKeys) {
		return Flux.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findAll((qBeer.id.in(missingKeys))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).map(beerMapper::fromBeer).doOnNext(dbValue -> {
			if (redisEnabled)
				reactiveCacheService.put(dbValue.id(), dbValue).subscribe();
		});
	}

	public Mono<BeerDto> saveBeer(BeerDto beerDto) {

		var validateHeaders = validateContext();
		var persist = Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository
					.exists((qBeer.name.equalsIgnoreCase(beerDto.name()).or(qBeer.upc.equalsIgnoreCase(beerDto.upc())))
							.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx)))
							.and(qBeer.active.eq(true)));
		}).flatMap(exists -> exists ? Mono.error(new BusinessException(ExceptionReason.BEER_ALREADY_PRESENT))
				: Mono.just(beerDto)).map(beerMapper::fromBeerDto).flatMap(beerRepository::save)
				.map(beerMapper::fromBeer).doOnError(exc -> log.error("exception {}", exc));
		return validateHeaders.then(persist);

	}

	public Mono<BeerDto> updateBeer(String beerId, BeerDto beerDto) {
		var validateHeaders = validateContext();
		return validateHeaders.then(Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findOne((qBeer.id.eq(beerId))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).switchIfEmpty(Mono.error(new BusinessException(BEER_NOT_FOUND)))
				.map(beer -> beerMapper.fromBeerDto(beerDto, beer)).flatMap(beerRepository::save)
				.map(beerMapper::fromBeer));
	}

	public Mono<String> deleteById(String beerId) {
		var validateHeaders = validateContext();
		return validateHeaders.then(Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.exists((qBeer.id.eq(beerId))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).flatMap(__ -> {
			if (__.booleanValue())
				return beerRepository.deleteById(beerId).map(___ -> beerId);
			return Mono.error(new BusinessException(BEER_NOT_FOUND));
		}));
	}

	public Mono<BeerDto> findBeerByUpc(String upc) {
		var validateHeaders = validateContext();
		return validateHeaders.then(Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findOne((qBeer.upc.equalsIgnoreCase(upc))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).map(beerMapper::fromBeer));
	}

	public Flux<BeerDto> findAllBeer() {
		var validateHeaders = validateContext();
		return validateHeaders.thenMany(Flux.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findAll(
					qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx)).and(qBeer.active.eq(true)));
		}).map(beerMapper::fromBeer));
	}

	public Mono<SenderResult<Void>> consumeCheckInventoryEvent(CheckInventoryEvent event) {
		var validateHeaders = validateContext();
		return validateHeaders.then(Mono.deferContextual(ctx -> {
			QBeer qBeer = QBeer.beer;
			return beerRepository.findOne((qBeer.id.eq(event.beerId()))
					.and(qBeer.tenantId.eq(fetchHeaderFromContext.apply(TENANT_ID, ctx))).and(qBeer.active.eq(true)));
		}).flatMap(this::checkBeerInventory));

	}

	private Mono<SenderResult<Void>> checkBeerInventory(Beer beer) {
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
