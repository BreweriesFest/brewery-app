package com.brewery.app.inventory.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class BeerInventory extends Auditable {

    private String upc;
    private String beerId;
    @Setter
    private Integer quantityOnHand;

}
