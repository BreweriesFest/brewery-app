package com.brewery.inventory.repository;

import com.brewery.model.audit.Auditable;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Inventory extends Auditable {

	private String upc;

	private String beerId;

	@Setter
	private Integer qtyOnHand;

}
