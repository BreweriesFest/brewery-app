package com.brewery.app.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigDecimal;

public record BeerDto(@Null String id, @NotBlank String name, @NotBlank String upc, @NotNull BigDecimal price,
        @NotNull BeerStyle style) {
    public BeerDto(String id) {
        this(id, null, null, null, null);
    }
}
