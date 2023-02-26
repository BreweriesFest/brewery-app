package com.brewery.model.dto;

public record OrderLineDto(String beerId, int orderQuantity, int quantityAllocated) {
}
