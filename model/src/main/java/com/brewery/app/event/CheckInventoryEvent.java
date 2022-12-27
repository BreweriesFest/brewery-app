package com.brewery.app.event;

import com.brewery.app.domain.Record;

public record CheckInventoryEvent(String beerId) implements Record<String> {
    @Override
    public String key() {
        return beerId();
    }
}
