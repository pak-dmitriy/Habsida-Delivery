package com.delivery.habsida.dto;

import com.delivery.habsida.entity.DeliveryGeography;
import com.delivery.habsida.entity.DeliveryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record StoreDeliverySettingRequest(

        @NotNull
        Boolean deliveryEnabled,

        @NotNull (message = "Delivery can not be empty")
        DeliveryType deliveryType,

        @NotNull (message = "Min order price can not be empty")
        @PositiveOrZero (message = "Min order price must be zero or positive")
        BigDecimal minOrderPrice,

        @NotNull (message = "Free delivery threshold can not be empty")
        @PositiveOrZero (message = "Free delivery threshold price must be zero or positive")
        BigDecimal freeDeliveryThreshold,

        @NotNull (message = "Max distance can not be empty")
        @PositiveOrZero (message = "Max distance must be zero or positive")
        BigDecimal maxDistance,

        @NotNull (message = "Delivery geography can not be empty")
        DeliveryGeography deliveryGeography
) {
}
