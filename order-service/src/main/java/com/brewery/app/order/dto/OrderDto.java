package com.brewery.app.order.dto;

import com.brewery.app.order.repository.OrderStatus;

import java.util.Set;

public record OrderDto(String id, Set<OrderLineDto> orderLineSet, OrderStatus status) {
    public OrderDto withOrderLine(Set<OrderLineDto> orderLineSet) {
        return new OrderDto(id(), orderLineSet, status());
    }
}
