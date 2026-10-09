package com.delivery.habsida.dto;

import com.delivery.habsida.entity.DeliveryGeography;
import com.delivery.habsida.entity.DeliveryType;
import com.delivery.habsida.entity.StoreDeliverySetting;

import java.math.BigDecimal;

public record StoreDeliverySettingResponse (
        Long id,
        Long storeId,
        Boolean deliveryEnabled,
        DeliveryType deliveryType,
        BigDecimal minOrderPrice,
        BigDecimal freeDeliveryThreshold,
        BigDecimal maxDistance,
        DeliveryGeography deliveryGeography
) {
public static StoreDeliverySettingResponse from(StoreDeliverySetting storeDeliverySetting) {
    return new StoreDeliverySettingResponse(
            storeDeliverySetting.getId(),
            storeDeliverySetting.getStore().getId(),
            storeDeliverySetting.getDeliveryEnabled(),
            storeDeliverySetting.getDeliveryType(),
            storeDeliverySetting.getMinOrderPrice(),
            storeDeliverySetting.getFreeDeliveryThreshold(),
            storeDeliverySetting.getMaxDistance(),
            storeDeliverySetting.getDeliveryGeography()
    );
}
}
