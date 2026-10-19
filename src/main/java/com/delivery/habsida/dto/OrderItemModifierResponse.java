package com.delivery.habsida.dto;

import com.delivery.habsida.entity.OrderItemModifier;

import java.math.BigDecimal;

public record OrderItemModifierResponse(
        Long id,
        String optionName,
        BigDecimal priceDelta
        ) {
    public static OrderItemModifierResponse from(OrderItemModifier orderItemModifier) {
        return new OrderItemModifierResponse(
                orderItemModifier.getId(),
                orderItemModifier.getOptionName(),
                orderItemModifier.getPriceDelta()
        );
    }
}
