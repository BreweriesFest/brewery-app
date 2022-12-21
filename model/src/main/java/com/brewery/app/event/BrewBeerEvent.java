package com.brewery.app.event;

import com.brewery.app.domain.Record;

public record BrewBeerEvent(String id) implements Record<String> {
    @Override
    public String key() {
        return id();
    }
}
