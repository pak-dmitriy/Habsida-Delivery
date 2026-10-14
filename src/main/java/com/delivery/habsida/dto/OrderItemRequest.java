package com.delivery.habsida.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderItemRequest(
        @NotNull(message = "ProductId can not be empty")
        Long productId,

        @NotNull
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        List<@NotNull Long> modifierOptionIds
) {
}
