package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupWithOptionsDto;
import com.delivery.habsida.dto.ProductModifierGroupResponse;
import com.delivery.habsida.service.ProductModifierGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<ModifierGroupWithOptionsDto> getModifierGroupWithOptions(@PathVariable Long storeId,
                                                                         @PathVariable Long productId) {
        return productModifierGroupService.getModifierGroupWithOptions(storeId, productId);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteProductModifierGroup(@PathVariable Long storeId,
                                                           @PathVariable Long productId,
                                                           @PathVariable Long groupId) {
        productModifierGroupService.deleteProductModifierGroup(storeId, productId, groupId);
        return ResponseEntity.noContent().build();
    }
}
