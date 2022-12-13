package com.brewery.app.beer.service;

import com.brewery.app.beer.mapper.BeerMapper;
import com.brewery.app.beer.repository.Beer;
import com.brewery.app.beer.repository.BeerRepository;
import com.brewery.app.beer.repository.QBeer;
import com.brewery.app.exception.BusinessException;
import com.brewery.app.exception.ExceptionReason;
import com.brewery.app.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.brewery.app.exception.ExceptionReason.BEER_NOT_FOUND;
import static com.brewery.app.util.AppConstant.TENANT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    public Mono<BeerDto> findBeerById(String beerId) {
        return Mono.deferContextual(ctx -> {
            QBeer qBeer = QBeer.beer;
            return beerRepository.findOne((qBeer.id.eq(beerId)).and(qBeer.tenantId.eq((String) ctx.get(TENANT_ID)))
                    .and(qBeer.active.eq(true)));
        }).switchIfEmpty(Mono.just(new Beer())).map(beerMapper::fromBeer);
    }

    public Mono<BeerDto> saveBeer(BeerDto beerDto) {

        var validate = Mono.deferContextual(ctx -> {
            QBeer qBeer = QBeer.beer;
            return beerRepository
                    .exists((qBeer.name.equalsIgnoreCase(beerDto.name()).or(qBeer.upc.equalsIgnoreCase(beerDto.upc())))
                            .and(qBeer.tenantId.eq((String) ctx.get(TENANT_ID))).and(qBeer.active.eq(true)));
        }).handle((__, sink) -> {
            if (__.equals(Boolean.TRUE))
                sink.error(new BusinessException(ExceptionReason.BEER_ALREADY_PRESENT));
        });
        var persist = Mono.just(beerDto).map(beerMapper::fromBeerDto).flatMap(beerRepository::save)
                .map(beerMapper::fromBeer);
        return validate.then(persist).doOnError(exc -> log.error("exception {}", exc.getMessage()));

    }

    public Mono<BeerDto> updateBeer(String beerId, BeerDto beerDto) {

        return Mono.deferContextual(ctx -> {
            QBeer qBeer = QBeer.beer;
            return beerRepository.findOne((qBeer.id.eq(beerId)).and(qBeer.tenantId.eq((String) ctx.get(TENANT_ID)))
                    .and(qBeer.active.eq(true)));
        }).switchIfEmpty(Mono.error(new BusinessException(BEER_NOT_FOUND)))
                .map(beer -> beerMapper.fromBeerDto(beerDto, beer)).flatMap(beerRepository::save)
                .map(beerMapper::fromBeer);
    }

    public Mono<String> deleteById(String beerId) {
        return Mono.deferContextual(ctx -> {
            QBeer qBeer = QBeer.beer;
            return beerRepository.exists((qBeer.id.eq(beerId)).and(qBeer.tenantId.eq((String) ctx.get(TENANT_ID)))
                    .and(qBeer.active.eq(true)));
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
                    .and(qBeer.tenantId.eq((String) ctx.get(TENANT_ID))).and(qBeer.active.eq(true)));
        }).switchIfEmpty(Mono.just(new Beer())).map(beerMapper::fromBeer);
    }
}
