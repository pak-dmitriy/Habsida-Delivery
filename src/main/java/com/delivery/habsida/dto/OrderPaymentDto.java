package com.delivery.habsida.dto;

import com.delivery.habsida.entity.OrderPayment;
import com.delivery.habsida.entity.PaymentMethod;
import com.delivery.habsida.entity.PaymentStatus;

import java.time.LocalDateTime;

public record OrderPaymentDto(
        Long id,
        Long orderId,
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime paidAt,
        String provider,
        TransactionDto transaction
        ) {
    public static OrderPaymentDto from(OrderPayment orderPayment) {
        return new OrderPaymentDto(
                orderPayment.getId(),
                orderPayment.getOrder().getId(),
                orderPayment.getStatus(),
                orderPayment.getMethod(),
                orderPayment.getPaidAt(),
                orderPayment.getProvider(),
                orderPayment.getTransaction() != null
                ? TransactionDto.from(orderPayment.getTransaction()) : null
        );
    }
}
