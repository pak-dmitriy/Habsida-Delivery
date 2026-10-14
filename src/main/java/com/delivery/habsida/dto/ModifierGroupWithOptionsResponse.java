package com.delivery.habsida.dto;

import com.delivery.habsida.entity.ModifierGroup;

import java.util.List;

public record ModifierGroupWithOptionsDto(
        Long id,
        String name,
        boolean required,
        Integer minSelect,
        Integer maxSelect,
        List<ModifierOptionResponse> options
) {
    public static ModifierGroupWithOptionsDto from(ModifierGroup modifierGroup) {
        return new ModifierGroupWithOptionsDto(
                modifierGroup.getId(),
                modifierGroup.getName(),
                modifierGroup.isRequired(),
                modifierGroup.getMinSelect(),
                modifierGroup.getMaxSelect(),
                modifierGroup.getModifierOptions().stream().map(ModifierOptionResponse::from).toList()
        );
    }
}
