package com.brewery.app.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Defines the business exception reasons.
 */
@Getter
@AllArgsConstructor
public enum ExceptionReason implements ErrorPolicy {

	INVALID_SHOPPING_LIST_ID("Invalid shopping list id", HttpStatus.BAD_REQUEST, ExceptionType.ValidationException),
	INVALID_PRODUCT_ID("Invalid product id", HttpStatus.BAD_REQUEST, ExceptionType.ValidationException),
	TENANT_NOT_FOUND("Tenant Id not found ", HttpStatus.NOT_FOUND, ExceptionType.ValidationException),
	CUSTOMER_NOT_FOUND("Customer Id not found ", HttpStatus.NOT_FOUND, ExceptionType.ValidationException),
	SHOPPING_LIST_NAME_ALREADY_PRESENT("Shopping list name already present", HttpStatus.BAD_REQUEST,
			ExceptionType.ValidationException),
	SPECIAL_CHARACTERS_NOT_ALLOWED("Special Characters are not allowed as input", HttpStatus.BAD_REQUEST,
			ExceptionType.ValidationException),
	BEER_ALREADY_PRESENT("Beer already present", HttpStatus.BAD_REQUEST, ExceptionType.ValidationException),
	BEER_NOT_FOUND("Beer not found", HttpStatus.BAD_REQUEST, ExceptionType.ValidationException),
	INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, ExceptionType.ServerException),
	CUSTOMIZE_REASON("%s", HttpStatus.BAD_REQUEST, ExceptionType.ValidationException),

	INVENTORY_NOT_AVAILABLE("Inventory not available", HttpStatus.BAD_REQUEST, ExceptionType.ValidationException);

	private final String code = this.name();

	private final String message;

	private final HttpStatus httpStatus;

	private final ExceptionType errorType;

}