package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        int lowStockThreshold,
        ProductStatus status,
        int discountPercent,
        int maxQuantity,
        int minQuantity,
        Long storeId,
        Long categoryId

        ) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getLowStockThreshold(),
                product.getStatus(),
                product.getDiscountPercent(),
                product.getMaxQuantity(),
                product.getMinQuantity(),
                product.getStore().getId(),
                product.getCategory().getId()
        );
    }
}
