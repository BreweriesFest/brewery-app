package com.brewery.app.beer.resource;

import com.brewery.app.beer.service.BeerService;
import com.brewery.app.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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

    @QueryMapping
    Mono<String> hello() {
        return Mono.just("hello graphql");
    }
}
