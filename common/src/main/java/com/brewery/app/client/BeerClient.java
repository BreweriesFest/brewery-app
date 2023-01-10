package com.brewery.app.client;

import com.brewery.app.model.BeerDto;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.brewery.app.util.AppConstant.*;

@Service
@Slf4j
@Profile("beer-client-service")
public class BeerClient extends GraphqlClient {

	public BeerClient(@Value("${app.client.beer.url}") String url, HttpGraphQlClient httpGraphQlClient,
			ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory, Retry beerClientRetry) {
		super(httpGraphQlClient, url, reactiveCircuitBreakerFactory, beerClientRetry, RESILIENCE_ID_BEER_CLIENT);
	}

	public Flux<BeerDto> getBeerById(Collection<String> beerId) {

		final var query = """
				query($id: [String!]!) {
				    data: beerById(id: $id) {
				        id
				        name
				        upc
				        price
				        style
				    }
				}
				""";

		return fromFlux(List.of(TENANT_ID, CUSTOMER_ID), query, Map.of("id", beerId),
				new ParameterizedTypeReference<>() {
				});

	}

	public Flux<BeerDto> getAllByTenant() {

		final var query = """
				query{
				    data: beer {
				        id
				    }
				}
				""";

		return fromFlux(List.of(TENANT_ID, CUSTOMER_ID), query, Map.of(), new ParameterizedTypeReference<>() {
		});

	}

}
