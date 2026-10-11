package com.delivery.habsida.dto;

import com.delivery.habsida.entity.ProductImage;

public record ProductImageDto(
        Long id,
        String imagePhoto,
        int sortOrder
) {

    public static ProductImageDto from(ProductImage productImage) {
        return  new ProductImageDto(
                productImage.getId(),
                productImage.getImagePhoto(),
                productImage.getSortOrder()
        );
    }
}
