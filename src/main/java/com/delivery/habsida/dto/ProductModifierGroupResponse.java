package com.delivery.habsida.dto;

import com.delivery.habsida.entity.ProductModifierGroup;

public record ProductModifierGroupResponse(
        Long id,
        Long productId,
        Long modifierGroupId
) {
    public static ProductModifierGroupResponse from(ProductModifierGroup productModifierGroup) {
        return new ProductModifierGroupResponse(
                productModifierGroup.getId(),
                productModifierGroup.getProduct().getId(),
                productModifierGroup.getModifierGroup().getId()
        );
    }
}
