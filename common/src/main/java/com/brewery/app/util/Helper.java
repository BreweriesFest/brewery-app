package com.brewery.app.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.BiFunction;

@UtilityClass
public class Helper {

    public static final BiFunction<ServerWebExchange, String, String> getHeader = ((serverWebExchange,
            header) -> serverWebExchange.getRequest().getHeaders().get(header).stream().findFirst().orElse(null));
}
