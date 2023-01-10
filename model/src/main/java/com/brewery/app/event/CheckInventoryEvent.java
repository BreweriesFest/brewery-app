package com.brewery.app.event;

import com.brewery.app.domain.Record;

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
