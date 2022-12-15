package com.brewery.app.order.repository;

import com.brewery.app.domain.Auditable;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

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

    @Setter
    @DocumentReference
    private Order order;
}
