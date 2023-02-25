package com.brewery.order.repository;

import com.brewery.model.audit.Auditable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orderLine")
public class OrderLine extends Auditable {

	private String beerId;

	@Builder.Default
	private int orderQuantity = 0;

	@Builder.Default
	private int quantityAllocated = 0;

}
