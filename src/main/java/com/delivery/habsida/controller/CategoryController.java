package com.delivery.habsida.controller;

import com.delivery.habsida.dto.CategoryCreateRequest;
import com.delivery.habsida.dto.CategoryResponse;
import com.delivery.habsida.service.CategoryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory
            (@PathVariable Long storeId,
             @RequestBody @Valid CategoryCreateRequest categoryCreateRequest) {
        CategoryResponse created = categoryService.createCategory(storeId, categoryCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @GetMapping
    public Page<CategoryResponse> getCategories(@PathVariable Long storeId, @ParameterObject Pageable pageable, Authentication authentication) {
        return categoryService.getCategories(storeId, pageable, authentication);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory
            (@PathVariable Long storeId, @PathVariable Long categoryId,
             @RequestBody @Valid CategoryCreateRequest categoryCreateRequest) {
        CategoryResponse update = categoryService.updateCategory(storeId, categoryId, categoryCreateRequest);
        return ResponseEntity.ok().body(update);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long storeId,
                                               @PathVariable Long categoryId) {
        categoryService.deleteCategory(storeId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
