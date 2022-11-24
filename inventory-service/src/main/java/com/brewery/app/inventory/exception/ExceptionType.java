package com.brewery.app.inventory.exception;

import graphql.ErrorClassification;

public enum ExceptionType implements ErrorClassification {

    ValidationException, BusinessError, ServerException
}
