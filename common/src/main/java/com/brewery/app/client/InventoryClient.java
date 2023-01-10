package com.brewery.app.client;

import com.brewery.app.domain.InventoryDTO;
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
@Profile("inventory-client-service")
public class InventoryClient extends GraphqlClient {

	public InventoryClient(@Value("${app.client.inventory.url}") String url, HttpGraphQlClient httpGraphQlClient,
			ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory, Retry inventoryClientRetry) {
		super(httpGraphQlClient, url, reactiveCircuitBreakerFactory, inventoryClientRetry,
				RESILIENCE_ID_INVENTORY_CLIENT);
	}

	public Flux<InventoryDTO> getInventoryByBeerId(Collection<String> beerId) {

		final var query = """
				query($id: [String!]!) {
				    data: inventory(beerId: $id) {
				        id
				        beerId
				        qtyOnHand
				    }
				}
				""";

		return fromFlux(List.of(TENANT_ID, CUSTOMER_ID), query, Map.of("id", beerId),
				new ParameterizedTypeReference<>() {
				});

	}

}
