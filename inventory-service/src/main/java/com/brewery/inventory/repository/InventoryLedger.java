package com.brewery.inventory.repository;

import com.brewery.model.audit.Auditable;
import com.brewery.model.dto.InventoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class InventoryLedger extends Auditable {

	private String inventoryId;

	private InventoryType type;

	private String referenceId;

	private Integer qty;

	private Integer totQty;

}
