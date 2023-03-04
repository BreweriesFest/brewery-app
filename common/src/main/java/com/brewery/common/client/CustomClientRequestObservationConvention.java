package com.brewery.common.client;

import io.micrometer.common.KeyValue;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientHttpObservationDocumentation;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientRequestObservationContext;
import org.springframework.web.reactive.function.client.DefaultClientRequestObservationConvention;

import java.util.regex.Pattern;

@Component
public class CustomClientRequestObservationConvention extends DefaultClientRequestObservationConvention {

	private static final String ROOT_PATH = "/";

	private static final Pattern PATTERN_BEFORE_PATH = Pattern.compile("^https?://[^/]+/");

	private static final KeyValue URI_NONE = KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.URI,
			KeyValue.NONE_VALUE);

	private static final KeyValue URI_ROOT = KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.URI,
			ROOT_PATH);

	private static String extractPath(String uriTemplate) {
		String path = PATTERN_BEFORE_PATH.matcher(uriTemplate).replaceFirst("");
		return (path.startsWith("/") ? path : "/" + path);
	}

	@Override
	protected KeyValue uri(ClientRequestObservationContext context) {
		if (context.getUriTemplate() != null) {
			return KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.URI,
					extractPath(context.getUriTemplate()));
		}
		ClientRequest request = context.getRequest();
		if (request != null && ROOT_PATH.equals(request.url().getPath())) {
			return URI_ROOT;
		}
		if (request != null) {
			return KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.URI,
					extractPath(request.url().getPath()));
		}
		return URI_NONE;
	}

}
