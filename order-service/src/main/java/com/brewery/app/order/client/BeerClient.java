package com.brewery.app.order.client;

import com.brewery.app.exception.BusinessException;
import com.brewery.app.exception.ExceptionReason;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

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

    public Mono<Collection<BeerDto>> getBeerById(Collection<String> beerId) {

        final var query = """
                query($id: [String!]!)  {
                    data: beerById(id: $id) {
                        id, name, upc, price, style
                    }
                }
                """;
        var variables = new HashMap<String, Object>();
        variables.put("id", beerId);

        final String document = """
                query{
                    data: beerById(id:["639f52147214bb4b28ab2af8"]) {
                        id, name, upc, price, style
                    }
                }
                """;

        var req = GraphqlRequest.builder().query(query).variables(variables).build();

        // return httpGraphQlClient.mutate().url(url).headers(httpHeaders -> {
        // httpHeaders.add("tenantId", "txt");
        // httpHeaders.add("customerId", "test");
        // }).build().document(document).retrieve("data").toEntity(new ParameterizedTypeReference<>() {
        // });
        return webClient.post().uri(url).bodyValue(req).headers(httpHeaders -> {
            httpHeaders.add("tenantId", "txt");
            httpHeaders.add("customerId", "test");
        }).retrieve().bodyToMono(new ParameterizedTypeReference<GraphqlResponse<Collection<BeerDto>>>() {
        }).map(GraphqlResponse::getData).map(GraphqlResponse.Data::getData)
                .onErrorResume(ex -> Mono.error(new BusinessException(ExceptionReason.INTERNAL_SERVER_ERROR)));
    }
}
