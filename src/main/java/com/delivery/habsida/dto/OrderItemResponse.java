package com.delivery.habsida.dto;

import com.delivery.habsida.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemResponse(
        Long id,
        Long orderId,
        String productName,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal subtotal,
        Integer discountPercent,
        BigDecimal discountAmount,
        List<OrderItemModifierResponse> orderItemModifiers
        ) {

    public static OrderItemResponse from(OrderItem orderItem, List<OrderItemModifierResponse> orderItemModifiers) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getOrder().getId(),
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getQuantity(),
                orderItem.getSubtotal(),
                orderItem.getDiscountPercent(),
                orderItem.getDiscountAmount(),
                orderItemModifiers
        );
    }
}
