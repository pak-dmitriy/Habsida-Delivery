package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Status;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.TypeStoreServices;

import java.math.BigDecimal;

public record StoreResponse(
        Long id,
        String name,
        String storeSlug,
        String description,
        TypeStoreServices typeStoreServices,
        String phone,
        String logo,
        Status status,
        String pickupAddress,
        BigDecimal deliveryFee
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getStoreSlug(),
                store.getDescription(),
                store.getTypeStoreServices(),
                store.getPhone(),
                store.getLogo(),
                store.getStatus(),
                store.getPickupAddress(),
                store.getDeliveryFee()
        );
    }
}
