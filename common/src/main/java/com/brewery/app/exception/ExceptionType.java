package com.brewery.app.exception;

import graphql.ErrorClassification;

public enum ExceptionType implements ErrorClassification {

	ValidationException, BusinessError, ServerException

}
