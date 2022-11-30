package com.brewery.app.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.language.SourceLocation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Getter
public class BusinessException extends RuntimeException implements GraphQLError, ErrorPolicy {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    protected final String code;
    protected final String message;
    protected final HttpStatus httpStatus;
    protected final ExceptionType errorType;

    /**
     * Constructor accepting an exception reason.
     *
     * @param reason
     *            the reason of the exception
     */
    public BusinessException(final ExceptionReason reason) {
        super(reason.getMessage());
        this.code = reason.getCode();
        this.message = reason.getMessage();
        this.httpStatus = reason.getHttpStatus();
        this.errorType = reason.getErrorType();
    }

    public BusinessException(final ExceptionReason reason, final String message) {
        super(reason.getMessage());
        this.code = reason.getCode();
        this.message = message;
        this.httpStatus = reason.getHttpStatus();
        this.errorType = reason.getErrorType();
    }

    /**
     * Constructor accepting an exception reason and an http status that will override the default one from the reason.
     *
     * @param reason
     *            the reason of the exception
     * @param overridingHttpStatus
     *            the http status which overrides the one from the reason
     */
    public BusinessException(final ExceptionReason reason, final HttpStatus overridingHttpStatus) {
        this.code = reason.getCode();
        this.message = reason.getMessage();
        this.httpStatus = overridingHttpStatus;
        this.errorType = reason.getErrorType();
    }

    /**
     * Constructor accepting an excepting reason and optional parameters which are replaced in the message.
     *
     * @param reason
     *            the reason of the exception
     * @param parameters
     *            the optional parameters
     */
    public BusinessException(final ExceptionReason reason, final Object... parameters) {
        if (parameters != null) {
            this.message = format(reason.getMessage(), parameters);
        } else {
            this.message = reason.getMessage();
        }

        this.code = reason.getCode();
        this.httpStatus = reason.getHttpStatus();
        this.errorType = reason.getErrorType();

    }

    public BusinessException(final ExceptionReason reason, final HttpStatus overridingHttpStatus,
            final ExceptionType errorType, final Object... parameters) {
        if (parameters != null) {
            this.message = format(reason.getMessage(), parameters);
        } else {
            this.message = reason.getMessage();
        }

        this.code = reason.getCode();
        this.httpStatus = overridingHttpStatus;
        this.errorType = errorType;

    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public String toString() {
        return format("BusinessException(code=%s, message=%s, httpStatus=%s)", this.getCode(), this.getMessage(),
                this.getHttpStatus().value());
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> customAttributes = new LinkedHashMap<>();
        customAttributes.put("errorCode", this.httpStatus.value());
        return customAttributes;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public Map<String, Object> toSpecification() {
        return GraphqlErrorHelper.toSpecification(this);
    }

    @Override
    public ErrorClassification getErrorType() {
        return errorType;
    }
}
