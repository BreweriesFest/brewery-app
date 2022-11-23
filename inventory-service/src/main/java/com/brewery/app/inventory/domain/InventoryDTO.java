package com.brewery.app.inventory.domain;

public record InventoryDTO(String id, String upc, String beerId, Integer quantityOnHand) {
}
