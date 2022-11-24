package com.brewery.app.inventory.exception;

import graphql.ErrorClassification;

public enum ErrorType implements ErrorClassification {

    ValidationError, BusinessError, ServerError
}
