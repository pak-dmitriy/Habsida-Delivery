package com.delivery.habsida.dto;

import com.delivery.habsida.entity.OrderPayment;
import com.delivery.habsida.entity.PaymentMethod;
import com.delivery.habsida.entity.PaymentStatus;

import java.time.LocalDateTime;

public record OrderPaymentResponse(
        Long id,
        Long orderId,
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime paidAt,
        String provider,
        TransactionResponse transaction
        ) {
    public static OrderPaymentResponse from(OrderPayment orderPayment) {
        return new OrderPaymentResponse(
                orderPayment.getId(),
                orderPayment.getOrder().getId(),
                orderPayment.getStatus(),
                orderPayment.getMethod(),
                orderPayment.getPaidAt(),
                orderPayment.getProvider(),
                orderPayment.getTransaction() != null
                ? TransactionResponse.from(orderPayment.getTransaction()) : null
        );
    }
}
