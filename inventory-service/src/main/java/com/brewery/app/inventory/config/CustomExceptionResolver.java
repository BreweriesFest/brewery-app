package com.brewery.app.inventory.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CustomExceptionResolver implements DataFetcherExceptionResolver {
    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment environment) {
        GraphQLError error = null;
        if (exception instanceof BadRequestException)
            error = GraphqlErrorBuilder.newError().errorType(((BadRequestException) exception).getErrorType())
                    .message(exception.getMessage()).build();

        return Mono.just(List.of(error));
    }
}

// private GraphQLError getNested(GraphQLError error) {
// if (error instanceof ExceptionWhileDataFetching) {
// ExceptionWhileDataFetching exceptionError = (ExceptionWhileDataFetching) error;
// if (exceptionError.getException() instanceof GraphQLError) {
// return (GraphQLError) exceptionError.getException();
// }
// }
// return error;
// }
// }
