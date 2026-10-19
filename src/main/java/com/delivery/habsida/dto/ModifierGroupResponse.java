package com.delivery.habsida.dto;

import com.delivery.habsida.entity.ModifierGroup;

public record ModifierGroupResponse(

        Long id,
        Long storeId,
        String name,
        boolean required,
        Integer minSelect,
        Integer maxSelect

) {
    public static ModifierGroupResponse from (ModifierGroup modifierGroup) {
return new ModifierGroupResponse(
        modifierGroup.getId(),
        modifierGroup.getStore().getId(),
        modifierGroup.getName(),
        modifierGroup.isRequired(),
        modifierGroup.getMinSelect(),
        modifierGroup.getMaxSelect()
);
    }
}
