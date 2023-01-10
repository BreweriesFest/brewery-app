package com.brewery.app.event;

import com.brewery.app.domain.Record;
import com.brewery.app.model.OrderDto;

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
