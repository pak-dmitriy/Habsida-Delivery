package com.delivery.habsida.service;

import com.delivery.habsida.dto.CategoryCreateRequest;
import com.delivery.habsida.dto.CategoryResponse;
import com.delivery.habsida.entity.Category;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.CategoryNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.CategoryRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;



@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public CategoryService(CategoryRepository categoryRepository, StoreRepository storeRepository) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public Page<CategoryResponse> getCategories(Long storeId, Pageable pageable, Authentication authentication) {
        return categoryRepository.findByStoreId(storeId, pageable)
                .map(CategoryResponse::from);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public CategoryResponse createCategory(Long storeId, CategoryCreateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setCategoryPhoto(request.categoryPhoto());
        category.setStore(store);

        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public CategoryResponse updateCategory(Long storeId, Long categoryId, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!category.getStore().getId().equals(storeId)) {
            throw new CategoryNotFoundException("Category not found");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        category.setCategoryPhoto(request.categoryPhoto());

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteCategory(Long storeId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!category.getStore().getId().equals(storeId)) {
            throw new CategoryNotFoundException("Category not found");
        }
        categoryRepository.delete(category);
    }
}
