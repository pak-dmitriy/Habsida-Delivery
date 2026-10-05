package com.delivery.habsida.dto;

import com.delivery.habsida.entity.PaymentMethod;
import com.delivery.habsida.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record OrderPaymentRequest(

        Long transactionId,

        @NotNull
        PaymentStatus status,

        PaymentMethod method
) {
}
