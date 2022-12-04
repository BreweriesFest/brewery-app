package com.brewery.app.util;

import lombok.experimental.UtilityClass;
import org.springframework.graphql.server.WebGraphQlRequest;

import java.util.function.BiFunction;

@UtilityClass
public class Helper {

    public static final BiFunction<WebGraphQlRequest, String, String> getHeader = ((request, header) -> request
            .getHeaders().get(header).stream().findFirst().orElse(null));
}
