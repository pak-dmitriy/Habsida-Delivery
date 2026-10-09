package com.delivery.habsida.dto;

import com.delivery.habsida.entity.StoreDeliveryArea;

import java.math.BigDecimal;

public record StoreDeliveryAreaResponse(
        Long id,
        Long storeId,
        String districtName,
        BigDecimal deliveryFee
) {
    public static StoreDeliveryAreaResponse from (StoreDeliveryArea storeDeliveryArea) {
        return new StoreDeliveryAreaResponse(
                storeDeliveryArea.getId(),
                storeDeliveryArea.getStore().getId(),
                storeDeliveryArea.getDistrictName(),
                storeDeliveryArea.getDeliveryFee()
        );
    }
}
