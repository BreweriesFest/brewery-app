package com.brewery.app.inventory.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Defines the business exception reasons.
 */
@Getter
@AllArgsConstructor
public enum ErrorReason implements ErrorPolicy {
    INVALID_SHOPPING_LIST_ID("Invalid shopping list id", HttpStatus.BAD_REQUEST, ErrorType.ValidationError),
    INVALID_PRODUCT_ID("Invalid product id", HttpStatus.BAD_REQUEST, ErrorType.ValidationError),
    TENANT_NOT_FOUND("Tenant Id not found ", HttpStatus.NOT_FOUND, ErrorType.ValidationError),
    CUSTOMER_NOT_FOUND("Customer Id not found ", HttpStatus.NOT_FOUND, ErrorType.ValidationError),
    SHOPPING_LIST_NAME_ALREADY_PRESENT("Shopping list name already present", HttpStatus.BAD_REQUEST,
            ErrorType.ValidationError),
    SPECIAL_CHARACTERS_NOT_ALLOWED("Special Characters are not allowed as input", HttpStatus.BAD_REQUEST,
            ErrorType.ValidationError),
    INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.ServerError),
    CUSTOMIZE_REASON("%s", HttpStatus.BAD_REQUEST, ErrorType.ValidationError);

    private final String code = this.name();
    private final String message;
    private final HttpStatus httpStatus;
    private final ErrorType errorType;

}