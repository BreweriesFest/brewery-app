package com.brewery.app.order.client;

import com.brewery.app.model.BeerDto;
import com.brewery.app.response.GraphqlRequest;
import com.brewery.app.response.GraphqlResponse;
import com.brewery.app.util.GraphqlSchemaReaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collection;

@Service
@Slf4j
public class BeerClient {
    private final String url;
    private final WebClient webClient;

    private final HttpGraphQlClient httpGraphQlClient;

    public BeerClient(@Value("${app.client.beer.url}") String url, WebClient webClient,
            HttpGraphQlClient httpGraphQlClient) {
        this.url = url;
        this.webClient = webClient;
        this.httpGraphQlClient = httpGraphQlClient;
    }

    public Mono<Collection<BeerDto>> getAllBeer() throws IOException {

        final String query = GraphqlSchemaReaderUtil.getSchemaFromFileName("client/findBeer/findAllBeer");

        var request = GraphqlRequest.builder().query(query).build();

        return httpGraphQlClient.mutate().url(url).headers(httpHeaders -> {
            httpHeaders.add("tenantId", "txt");
            httpHeaders.add("customerId", "test");
        }).build().document(query).retrieve("data").toEntity(new ParameterizedTypeReference<>() {
        });

        // return webClient.post().uri(url).bodyValue(request)
        // .headers(httpHeaders -> {
        // httpHeaders.add("tenantId","txt");
        // httpHeaders.add("customerId","test");
        // })
        // .retrieve()
        // .bodyToMono(new ParameterizedTypeReference<GraphqlResponse<Collection<BeerDto>>>() {})
        // .map(GraphqlResponse::getData).map(GraphqlResponse.Data::getData);
    }
}
