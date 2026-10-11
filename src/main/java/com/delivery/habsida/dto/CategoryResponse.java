package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        String categoryPhoto,
        Long storeId
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCategoryPhoto(),
                category.getStore().getId()
        );
    }


}

