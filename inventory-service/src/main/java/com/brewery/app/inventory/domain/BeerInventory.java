package com.brewery.app.inventory.domain;

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
public class BeerInventory extends Auditable {

    private String upc;
    private String beerId;
    private Integer quantityOnHand;

}
