package com.brewery.beer.repository;

import com.brewery.model.audit.Auditable;
import com.brewery.model.dto.BeerStyle;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Beer extends Auditable {

	@Setter
	private String name;

	private String upc;

	@Setter
	private BigDecimal price;

	@Setter
	private BeerStyle style;

	@Setter
	@Builder.Default
	private Integer minQty = 0;

	@Setter
	@Builder.Default
	private boolean active = true;

}
