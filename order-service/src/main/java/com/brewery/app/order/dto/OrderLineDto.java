package com.brewery.app.order.dto;

public record OrderLineDto(String beerId, int orderQuantity, int quantityAllocated) {
}
