package com.brewery.common.exception;

import graphql.ErrorClassification;

public enum ExceptionType implements ErrorClassification {

	ValidationException, BusinessError, ServerException

}
