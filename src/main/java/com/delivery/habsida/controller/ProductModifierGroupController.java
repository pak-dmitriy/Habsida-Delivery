package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupWithOptionsResponse;
import com.delivery.habsida.dto.ProductModifierGroupResponse;
import com.delivery.habsida.service.ProductModifierGroupService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/products/{productId}/modifier-groups")
public class ProductModifierGroupController {
    private final ProductModifierGroupService productModifierGroupService;

    public ProductModifierGroupController(ProductModifierGroupService productModifierGroupService) {
        this.productModifierGroupService = productModifierGroupService;
    }

    @PostMapping("/{groupId}")
    public ResponseEntity<ProductModifierGroupResponse> createProductModifierGroup(@PathVariable Long storeId,
                                                                                   @PathVariable Long productId,
                                                                                   @PathVariable Long groupId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productModifierGroupService.createProductModifierGroup(storeId, productId, groupId));
    }

    @GetMapping
    public Page<ModifierGroupWithOptionsResponse> getModifierGroupWithOptions(@PathVariable Long storeId,
                                                                              @PathVariable Long productId,
                                                                              @ParameterObject Pageable pageable) {
        return productModifierGroupService.getModifierGroupWithOptions(storeId, productId, pageable);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteProductModifierGroup(@PathVariable Long storeId,
                                                           @PathVariable Long productId,
                                                           @PathVariable Long groupId) {
        productModifierGroupService.deleteProductModifierGroup(storeId, productId, groupId);
        return ResponseEntity.noContent().build();
    }
}
