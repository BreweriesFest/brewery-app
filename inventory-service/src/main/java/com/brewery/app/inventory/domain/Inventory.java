package com.brewery.app.inventory.domain;

public record Inventory(String upc, String beerId, Integer quantityOnHand) {
}
