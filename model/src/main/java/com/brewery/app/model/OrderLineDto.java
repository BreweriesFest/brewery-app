package com.brewery.app.model;

public record OrderLineDto(String beerId, int orderQuantity, int quantityAllocated) {
}
