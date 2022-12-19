package com.brewery.app.order.repository;

import com.brewery.app.audit.Auditable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Customer extends Auditable {

    private String name;

    @Builder.Default
    @DocumentReference(lazy = true, lookup = "{ 'customer' : ?#{#self._id} }")
    @ReadOnlyProperty
    private Set<Order> orders = new HashSet<>();
}
