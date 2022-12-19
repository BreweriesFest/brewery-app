package com.brewery.app.inventory.repository;

import com.brewery.app.audit.Auditable;
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
