package com.delivery.habsida.dto;

import com.delivery.habsida.entity.OrderType;
import com.delivery.habsida.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(

        @NotNull(message = "Order type can not be empty")
        OrderType type,

        @NotNull(message = "PaymentMethod can not be empty")
        PaymentMethod method,

        String customerNote,

        @NotNull(message = "CustomerID can not be empty")
        Long customerId,

        @NotNull(message = "Customer address ID can not be empty")
        Long customerAddressId,

        @Valid
        @NotEmpty(message = "Order items can not be empty")
        List<OrderItemRequest> items
) {
}
