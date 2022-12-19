package com.brewery.app.util;

import lombok.experimental.UtilityClass;
import org.springframework.graphql.server.WebGraphQlRequest;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@UtilityClass
public class Helper {

    public static final BiFunction<WebGraphQlRequest, String, Optional<String>> getHeader = ((request,
            header) -> collectionAsStream(request.getHeaders().get(header)).findFirst());

    public static final <T> Stream<T> collectionAsStream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }
}
