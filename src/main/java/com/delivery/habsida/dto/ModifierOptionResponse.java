package com.delivery.habsida.dto;

import com.delivery.habsida.entity.ModifierOption;

import java.math.BigDecimal;

public record ModifierOptionResponse(

        Long id,
        Long modifierGroupId,
        String name,
        BigDecimal priceDelta,
        boolean isFree

) {
    public static ModifierOptionResponse from(ModifierOption option) {
        return new ModifierOptionResponse(
                option.getId(),
                option.getModifierGroup().getId(),
                option.getName(),
                option.getPriceDelta(),
                option.isFree()
        );
    }
}
