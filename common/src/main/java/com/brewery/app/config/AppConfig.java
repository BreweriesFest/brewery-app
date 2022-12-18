package com.brewery.app.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Value("${app.default.timezone}")
    private String timezone;

    @PostConstruct
    public void init() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
                .responseTimeout(Duration.ofMillis(20000)).compress(true)
                .doOnConnected(__ -> __.addHandlerLast(new ReadTimeoutHandler(20000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(20000, TimeUnit.MILLISECONDS)));
    }

    @Bean
    public WebClient webClient(HttpClient httpClient) {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    @Bean
    public HttpGraphQlClient httpGraphQlClient(WebClient webClient) {
        return HttpGraphQlClient.builder(webClient).build();
    }
}
