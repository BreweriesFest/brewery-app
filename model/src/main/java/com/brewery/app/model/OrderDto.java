package com.brewery.app.model;

import java.util.Collection;

public record OrderDto(String id, Collection<String> orderLineId, Collection<OrderLineDto> orderLine,
		OrderStatus status) {

	public OrderDto fromOrderLine(Collection<OrderLineDto> orderLine) {
		return new OrderDto(id(), orderLineId(), orderLine, status());
	}
}
