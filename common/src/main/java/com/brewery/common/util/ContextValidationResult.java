package com.brewery.common.util;

import lombok.Getter;

@Getter
public enum ContextValidationResult {

	SUCCESS, INVALID_TENANT_ID, INVALID_CUSTOMER_ID;

}
