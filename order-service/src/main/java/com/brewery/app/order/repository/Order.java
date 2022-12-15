package com.brewery.app.order.repository;

import com.brewery.app.domain.Auditable;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "order")
public class Order extends Auditable {

    // @DBRef(lazy = true)
    @Setter
    @DocumentReference
    @Builder.Default
    private Set<OrderLine> orderLineSet = new HashSet<>();
    @Setter
    private OrderStatus status;

    @DocumentReference(lazy = true)
    private Customer customer;
}
