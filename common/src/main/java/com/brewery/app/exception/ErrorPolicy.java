package com.brewery.app.exception;

import graphql.ErrorClassification;
import org.springframework.http.HttpStatus;

/**
 * Defines the policy contract for custom exceptions which need to adhere to it for providing a standardized behavior.
 */
public interface ErrorPolicy {

    /**
     * Get the exception code.
     *
     * @return the exception code.
     */
    String getCode();

    /**
     * Get the exception message.
     *
     * @return the exception message.
     */
    String getMessage();

    ErrorClassification getErrorType();

    HttpStatus getHttpStatus();

}