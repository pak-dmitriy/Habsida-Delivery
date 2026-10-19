package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ProductCreateRequest;
import com.delivery.habsida.dto.ProductResponse;
import com.delivery.habsida.entity.ProductStatus;
import com.delivery.habsida.service.ProductService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@PathVariable Long storeId,
                                                         @RequestBody @Valid ProductCreateRequest request) {
        ProductResponse created = productService.createProduct(storeId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Page<ProductResponse> getProducts(@PathVariable Long storeId,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) ProductStatus status, @ParameterObject
                                             Pageable pageable) {
        return productService.getProducts(storeId, categoryId, status, pageable);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long storeId,
                                      @PathVariable Long productId) {
        return productService.getProduct(storeId, productId);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long storeId,
                                                         @PathVariable Long productId,
                                                         @RequestBody @Valid ProductCreateRequest request) {
        ProductResponse updatedProduct = productService.updateProduct(storeId, productId, request);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long storeId,
                                              @PathVariable Long productId) {
        productService.deleteProduct(storeId, productId);

        return ResponseEntity.noContent().build();
    }
}
