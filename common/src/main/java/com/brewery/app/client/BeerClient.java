package com.brewery.app.client;

import com.brewery.app.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;

@Service
@Slf4j
@Profile("beer-client-service")
public class BeerClient extends GraphqlClient {

    public BeerClient(@Value("${app.client.beer.url}") String url, HttpGraphQlClient httpGraphQlClient) {
        super(httpGraphQlClient, url);
    }

    public Mono<Collection<BeerDto>> getAllBeer() {

        final var query = """
                query {
                  data: beer {
                    id, name, upc, style
                  }
                }
                """;

        return getHttpGraphQlResponse(List.of(TENANT_ID, CUSTOMER_ID), query);
    }

    public Mono<Collection<BeerDto>> getBeerById(Collection<String> beerId) {

        final var query = """
                query($id: [String!]!)  {
                    data: beerById(id: $id) {
                        id, name, upc, price, style
                    }
                }
                """;

        return getHttpGraphQlResponse(List.of(TENANT_ID, CUSTOMER_ID), query, Map.of("id", beerId));

    }
}
