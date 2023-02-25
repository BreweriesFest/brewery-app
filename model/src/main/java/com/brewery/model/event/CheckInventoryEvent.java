package com.brewery.model.event;

import com.brewery.model.domain.Record;

public record CheckInventoryEvent(String id, String beerId) implements Record<String>, Event {
	@Override
	public String key() {
		return beerId();
	}

	@Override
	public String getId() {
		return id();
	}
}
