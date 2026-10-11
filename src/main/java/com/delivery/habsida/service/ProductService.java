package com.delivery.habsida.service;

import com.delivery.habsida.dto.ProductCreateRequest;
import com.delivery.habsida.dto.ProductResponse;
import com.delivery.habsida.entity.Category;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductStatus;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.CategoryNotFoundException;
import com.delivery.habsida.exception.InvalidQuantityException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.CategoryRepository;
import com.delivery.habsida.repository.ProductRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          StoreRepository storeRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
    }

    private Product getProductOrThrow(Long storeId, Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (!product.getStore().getId().equals(storeId)) {
            throw new ProductNotFoundException("Product not found");
        }
        return product;
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ProductResponse createProduct(Long storeId, ProductCreateRequest request) {
        if (request.minQuantity() > request.maxQuantity()) {
            throw new InvalidQuantityException("MinQuantity cannot be greater than MaxQuantity");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!category.getStore().getId().equals(storeId)) {
            throw new CategoryNotFoundException("Category not found");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setLowStockThreshold(request.lowStockThreshold());
        product.setStatus(request.status());
        product.setDiscountPercent(request.discountPercent());
        product.setMaxQuantity(request.maxQuantity());
        product.setMinQuantity(request.minQuantity());
        product.setStore(store);
        product.setCategory(category);

        return ProductResponse.from(productRepository.save(product));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public Page<ProductResponse> getProducts(Long storeId, Long categoryId, ProductStatus status, Pageable pageable) {
        if (categoryId != null && status != null) {
            return productRepository.findByStoreIdAndCategoryIdAndStatus(storeId, categoryId, status, pageable)
                    .map(ProductResponse::from);

        } else if (categoryId != null) {
            return productRepository.findByStoreIdAndCategoryId(storeId, categoryId, pageable)
                    .map(ProductResponse::from);

        } else if (status != null) {
            return productRepository.findByStoreIdAndStatus(storeId, status, pageable)
                    .map(ProductResponse::from);
        } else {
            return productRepository.findByStoreId(storeId, pageable)
                    .map(ProductResponse::from);
        }
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ProductResponse getProduct(Long storeId, Long productId) {

        Product product = getProductOrThrow(storeId, productId);

        return ProductResponse.from(product);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ProductResponse updateProduct(Long storeId, Long productId, ProductCreateRequest request) {
        if (request.minQuantity() > request.maxQuantity()) {
            throw new InvalidQuantityException("MinQuantity cannot be greater than MaxQuantity");
        }

        Product product = getProductOrThrow(storeId, productId);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!category.getStore().getId().equals(storeId)) {
            throw new CategoryNotFoundException("Category not found");
        }
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setLowStockThreshold(request.lowStockThreshold());
        product.setStatus(request.status());
        product.setDiscountPercent(request.discountPercent());
        product.setMaxQuantity(request.maxQuantity());
        product.setMinQuantity(request.minQuantity());
        product.setCategory(category);

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteProduct(Long storeId, Long productId) {
        Product product = getProductOrThrow(storeId, productId);

        productRepository.delete(product);
    }
}


