package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ProductImageResponse;
import com.delivery.habsida.dto.ProductImageRequest;
import com.delivery.habsida.service.ProductImageService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/products/{productId}/images")
public class ProductImageController {
    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping
    public ResponseEntity<ProductImageResponse> createProductImage(@PathVariable Long storeId,
                                                                   @PathVariable Long productId,
                                                                   @RequestBody @Valid ProductImageRequest request) {
        ProductImageResponse created = productImageService.createImages(storeId, productId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Page<ProductImageResponse> getProductImages(@PathVariable Long storeId,
                                                       @PathVariable Long productId, @ParameterObject Pageable pageable) {
        return productImageService.getProductImages(storeId, productId, pageable);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteProductImages(@PathVariable Long storeId,
                                                    @PathVariable Long productId,
                                                    @PathVariable Long imageId) {
        productImageService.deleteProductImages(storeId, productId, imageId);

        return ResponseEntity.noContent().build();
    }
}
