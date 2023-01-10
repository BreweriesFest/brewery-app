package com.brewery.app.util;

import reactor.util.context.ContextView;

import java.util.function.Function;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.ContextValidator.ValidationResult.*;
import static com.brewery.app.util.Helper.*;

public interface ContextValidator extends Function<ContextView, ContextValidator.ValidationResult> {

	static ContextValidator isTenantIdValid() {
		return ctx -> {
			var tenantIdOpt = getHeader(TENANT_ID, ctx);
			var tenantId = tenantIdOpt.filter(isInstanceOfString).map(convertToString).filter(isNotBlankString);
			return tenantId.isPresent() ? SUCCESS : INVALID_TENANT_ID;
		};
	}

	static ContextValidator isCustomerIdValid() {
		return ctx -> {
			var tenantIdOpt = getHeader(CUSTOMER_ID, ctx);
			var tenantId = tenantIdOpt.filter(isInstanceOfString).map(convertToString).filter(isNotBlankString);
			return tenantId.isPresent() ? SUCCESS : INVALID_CUSTOMER_ID;
		};
	}

	default ContextValidator and(ContextValidator contextValidator) {
		return ctx -> {
			var result = this.apply(ctx);
			return result.equals(SUCCESS) ? contextValidator.apply(ctx) : result;
		};
	}

	enum ValidationResult {

		SUCCESS, INVALID_TENANT_ID, INVALID_CUSTOMER_ID

	}

}
