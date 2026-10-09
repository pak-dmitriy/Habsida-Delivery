package com.delivery.habsida.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StoreDeliveryAreaRequest(

        @NotBlank(message = "District name can not be empty")
        @Size(max = 100, message = "District name can not be longer than 100 characters")
        String districtName,

        @NotNull(message = "Delivery fee can not be empty")
        @PositiveOrZero(message = "Delivery fee must be positive or zero")
        BigDecimal deliveryFee
) {
    public StoreDeliveryAreaRequest {
        if (districtName != null) {
            districtName = districtName.strip();
        }
    }
}
