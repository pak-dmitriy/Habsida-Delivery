package com.delivery.habsida.service;

import com.delivery.habsida.dto.ProductImageDto;
import com.delivery.habsida.dto.ProductImageRequest;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductImage;
import com.delivery.habsida.exception.ImageNotFoundException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.repository.ProductImageRepository;
import com.delivery.habsida.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageService(ProductImageRepository productImageRepository, ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ProductImageDto createImages(Long storeId, Long productId, ProductImageRequest request) {

        Product product = getProductOrThrow(storeId, productId);

        ProductImage productImage = new ProductImage();
        productImage.setImagePhoto(request.imagePhoto());
        productImage.setSortOrder(request.sortOrder());
        productImage.setProduct(product);

        return ProductImageDto.from(productImageRepository.save(productImage));
    }


    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public Page<ProductImageDto> getProductImages(Long storeId, Long productId, Pageable pageable) {

        getProductOrThrow(storeId, productId);

        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId, pageable)
                .map(ProductImageDto::from);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteProductImages(Long storeId, Long productId, Long imageId) {

       getProductOrThrow(storeId, productId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ImageNotFoundException("Image not found");
        }

        productImageRepository.delete(image);
    }

    private Product getProductOrThrow(Long storeId, Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (!product.getStore().getId().equals(storeId)) {
            throw new ProductNotFoundException("Product not found");
        }
        return product;
    }
}
