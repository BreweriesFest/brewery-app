package com.brewery.app.exception;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.validation.ValidationErrorType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.brewery.app.util.Helper.collectionAsStream;

@Slf4j
@Component
public class ErrorInterceptor implements WebGraphQlInterceptor {

	@Override
	public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
		return chain.next(request).map(response -> {
			if (response.isValid()) {
				return response;
			}
			List<GraphQLError> graphQLErrors = collectionAsStream(response.getErrors())
					.filter(responseError -> !(responseError.getErrorType() instanceof ExceptionType))
					.map(this::resolveException).collect(Collectors.toList());

			if (!graphQLErrors.isEmpty()) {
				log.info("[ErrorInterceptor] Found invalid syntax error! Overriding the message.");
				return response.transform(builder -> builder.errors(graphQLErrors).build());
			}

			return response;
		});
	}

	private GraphQLError resolveException(ResponseError responseError) {

		ErrorClassification errorType = responseError.getErrorType();

		if (ErrorType.ValidationError.equals(errorType)) {
			String message = responseError.getMessage();
			log.info("[ErrorInterceptor] Returning invalid field error ");

			if (ValidationErrorType.MissingFieldArgument
					.equals(extractValidationErrorFromErrorMessage(responseError.getMessage()))) {
				String errorMessage = "Field " + StringUtils.substringBetween(message, "argument ", " @")
						+ " cannot be null";
				return new BusinessException(ExceptionReason.CUSTOMIZE_REASON, HttpStatus.BAD_REQUEST,
						ExceptionType.ValidationException, errorMessage);
			}
			if (ValidationErrorType.WrongType
					.equals(extractValidationErrorFromErrorMessage(responseError.getMessage()))) {
				String errorMessage = "Field " + StringUtils.substringBetween(message, "fields ", " @")
						+ " cannot be null";
				return new BusinessException(ExceptionReason.CUSTOMIZE_REASON, HttpStatus.BAD_REQUEST,
						ExceptionType.ValidationException, errorMessage);
			}
		}

		log.info("[ErrorInterceptor] Returning unknown query validation error ");
		return new BusinessException(ExceptionReason.CUSTOMIZE_REASON, HttpStatus.BAD_REQUEST,
				ExceptionType.ValidationException, responseError.getMessage());
	}

	private ValidationErrorType extractValidationErrorFromErrorMessage(String message) {
		return ValidationErrorType.valueOf(StringUtils.substringBetween(message, "type ", ":"));
	}

	//

}
