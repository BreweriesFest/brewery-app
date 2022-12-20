package com.brewery.app.util;

import com.brewery.app.exception.BusinessException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.graphql.server.WebGraphQlRequest;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.brewery.app.exception.ExceptionReason.CUSTOMIZE_REASON;
import static com.brewery.app.util.ContextValidator.ValidationResult.SUCCESS;
import static com.brewery.app.util.ContextValidator.isCustomerIdValid;
import static com.brewery.app.util.ContextValidator.isTenantIdValid;

@UtilityClass
public class Helper {

    public static final BiFunction<WebGraphQlRequest, String, Optional<String>> getHeader = ((request,
            header) -> collectionAsStream(request.getHeaders().get(header)).findFirst());
    public static final Predicate<Object> isInstanceOfString = String.class::isInstance;
    public static final Predicate<String> isNotBlankString = StringUtils::isNotBlank;
    public static final Function<Object, String> convertToString = String::valueOf;
    public static final BiFunction<String, ContextView, String> fetchHeaderFromContext = (header,
            ctx) -> getHeader(header, ctx).filter(isInstanceOfString).map(convertToString).filter(isNotBlankString)
                    .orElse(null);

    public static <T> Stream<T> collectionAsStream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

    public static Mono<Void> validateContext() {
        return Mono.deferContextual(__ -> {
            var result = isTenantIdValid().and(isCustomerIdValid()).apply(__);
            return SUCCESS.equals(result) ? Mono.empty()
                    : Mono.error(new BusinessException(CUSTOMIZE_REASON, result.name()));
        });
    }

    public static <T> Optional<T> getHeader(String header, ContextView ctx) {
        return ctx.getOrDefault(header, Optional.empty());
    }

    public static Map<String, Optional<String>> extractHeaders(Collection<String> headers, WebGraphQlRequest request) {
        var headerMap = new HashMap<String, Optional<String>>();
        headers.forEach(__ -> headerMap.put(__, getHeader.apply(request, __)));
        return headerMap;
    }

}
