package com.brewery.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;

public record BeerDto(@Null String id, @NotBlank String name, @NotBlank String upc, @NotNull BigDecimal price,
		@NotNull BeerStyle style, @NotNull Integer minQty) {
}
