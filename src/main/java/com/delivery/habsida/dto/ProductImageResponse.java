package com.delivery.habsida.dto;

import com.delivery.habsida.entity.ProductImage;

public record ProductImageResponse(
        Long id,
        String imagePhoto,
        int sortOrder
) {

    public static ProductImageResponse from(ProductImage productImage) {
        return  new ProductImageResponse(
                productImage.getId(),
                productImage.getImagePhoto(),
                productImage.getSortOrder()
        );
    }
}
