package com.brewery.app.inventory.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class BusinessExceptionResolver implements DataFetcherExceptionResolver {
    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment environment) {
        GraphQLError error;
        if (exception instanceof BusinessException)
            error = GraphqlErrorBuilder.newError().errorType(((BusinessException) exception).getErrorType())
                    .extensions(((BusinessException) exception).getExtensions()).message(exception.getMessage())
                    .build();
        else {
            var internalServerError = new BusinessException(ExceptionReason.INTERNAL_SERVER_ERROR);
            error = GraphqlErrorBuilder.newError().errorType(internalServerError.getErrorType())
                    .extensions(internalServerError.getExtensions()).message(exception.getMessage()).build();
        }

        return Mono.just(List.of(error));
    }
}