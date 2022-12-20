package com.brewery.app.beer.resource;

import com.brewery.app.beer.service.BeerService;
import com.brewery.app.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BeerController {

    private final BeerService beerService;

    @MutationMapping
    Mono<BeerDto> addBeer(@Argument BeerDto beerDto) {
        return beerService.saveBeer(beerDto);
    }

    @MutationMapping
    Mono<BeerDto> updateBeer(@Argument String beerId, @Argument BeerDto beerDto) {
        return beerService.updateBeer(beerId, beerDto);
    }

    @MutationMapping
    Mono<String> deleteBeer(@Argument String beerId) {
        return beerService.deleteById(beerId);
    }

    @QueryMapping
    Mono<BeerDto> beerByUpc(@Argument String upc) {
        return beerService.findBeerByUpc(upc);
    }

    @QueryMapping
    Flux<BeerDto> beerById(@Argument Collection<String> id) {
        return beerService.findBeerById(id).doOnError(ex -> log.error("", ex));
    }

    @QueryMapping
    Flux<BeerDto> beer() {
        return beerService.findAllBeer();
    }
}
