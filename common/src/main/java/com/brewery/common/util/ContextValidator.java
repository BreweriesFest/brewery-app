package com.brewery.common.util;

import reactor.util.context.ContextView;

import java.util.EnumSet;
import java.util.function.Function;

public interface ContextValidator extends Function<ContextView, EnumSet<ContextValidationResult>> {

	static final EnumSet<ContextValidationResult> SUCCESS_ONLY = EnumSet.of(ContextValidationResult.SUCCESS);

	static ContextValidator isTenantIdValid() {
		return ctx -> {
			var tenantIdOpt = Helper.getHeader(AppConstant.TENANT_ID, ctx);
			var tenantId = tenantIdOpt.filter(Helper.isInstanceOfString).map(Helper.convertToString)
					.filter(Helper.isNotBlankString);
			return tenantId.isPresent() ? SUCCESS_ONLY : EnumSet.of(ContextValidationResult.INVALID_TENANT_ID);
		};
	}

	static ContextValidator isCustomerIdValid() {
		return ctx -> {
			var tenantIdOpt = Helper.getHeader(AppConstant.CUSTOMER_ID, ctx);
			var tenantId = tenantIdOpt.filter(Helper.isInstanceOfString).map(Helper.convertToString)
					.filter(Helper.isNotBlankString);
			return tenantId.isPresent() ? SUCCESS_ONLY : EnumSet.of(ContextValidationResult.INVALID_CUSTOMER_ID);
		};
	}

	default ContextValidator and(ContextValidator contextValidator) {
		return ctx -> {
			var result = this.apply(ctx);
			var nextResult = contextValidator.apply(ctx);
			if (result.equals(SUCCESS_ONLY))
				return nextResult;
			if (nextResult.equals(SUCCESS_ONLY))
				return result;
			var combinedResult = EnumSet.copyOf(result);
			combinedResult.addAll(nextResult);
			return combinedResult;
		};
	}

}
