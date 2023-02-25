package com.brewery.model.event;

import com.brewery.model.domain.Record;
import com.brewery.model.dto.OrderDto;

public record OrderEvent(String id, OrderDto orderDto) implements Record<String>, Event {
	@Override
	public String key() {
		return orderDto().id();
	}

	@Override
	public String getId() {
		return id();
	}
}
