package com.delivery.habsida.dto;

import com.delivery.habsida.entity.StoreDeliveryRestriction;

public record StoreDeliveryRestrictionResponse(
        Long id,
        Long storeId,
        String restrictedArea
) {
    public static StoreDeliveryRestrictionResponse from (StoreDeliveryRestriction storeDeliveryRestriction) {
        return new StoreDeliveryRestrictionResponse(
                storeDeliveryRestriction.getId(),
                storeDeliveryRestriction.getStore().getId(),
                storeDeliveryRestriction.getRestrictedArea()
        );
    }
}
