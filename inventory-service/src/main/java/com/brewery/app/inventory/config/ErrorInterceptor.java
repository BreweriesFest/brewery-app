package com.brewery.app.inventory.config;

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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ErrorInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        return chain.next(request).map(response -> {
            log.info("[ErrorInterceptor] Intercepting response... ");

            List<GraphQLError> graphQLErrors = response.getErrors().stream().map(this::resolveException)
                    .collect(Collectors.toList());

            if (!graphQLErrors.isEmpty()) {
                log.info("[ErrorInterceptor] Found invalid syntax error! Overriding the message.");
                return response.transform(builder -> builder.errors(graphQLErrors));
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
                return new BadRequestException(errorMessage);
            }
            if (ValidationErrorType.WrongType
                    .equals(extractValidationErrorFromErrorMessage(responseError.getMessage()))) {
                String errorMessage = "Field " + StringUtils.substringBetween(message, "fields ", " @")
                        + " cannot be null";
                return new BadRequestException(errorMessage);
            }
        }

        log.info("[ErrorInterceptor] Returning unknown query validation error ");
        return new BadRequestException(responseError.getMessage());
    }

    private ValidationErrorType extractValidationErrorFromErrorMessage(String message) {
        return ValidationErrorType.valueOf(StringUtils.substringBetween(message, "type ", ":"));
    }

    //

}
