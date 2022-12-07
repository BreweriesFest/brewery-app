package com.brewery.app.domain;

public record InventoryDTO(String id, String upc, String beerId, Integer quantityOnHand) implements Record<String> {
    @Override
    public String key() {
        return beerId();
    }
}
