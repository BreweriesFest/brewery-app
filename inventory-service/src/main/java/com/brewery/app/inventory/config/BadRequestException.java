package com.brewery.app.inventory.config;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.language.SourceLocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class BadRequestException extends RuntimeException implements GraphQLError {

    private HttpStatus status = HttpStatus.BAD_REQUEST;

    private String message;

    public BadRequestException(String message) {
        this.message = message;
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> customAttributes = new LinkedHashMap<>();
        customAttributes.put("errorCode", this.status.value());
        return customAttributes;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.NullValueInNonNullableField;
    }

    @Override
    public Map<String, Object> toSpecification() {
        return GraphqlErrorHelper.toSpecification(this);
    }

}
