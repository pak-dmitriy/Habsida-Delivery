package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierGroupWithOptionsResponse;
import com.delivery.habsida.dto.ProductModifierGroupResponse;
import com.delivery.habsida.entity.ModifierGroup;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductModifierGroup;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.exception.ProductModifierGroupAlreadyExistsException;
import com.delivery.habsida.exception.ProductModifierGroupNotFoundException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.repository.ModifierGroupRepository;
import com.delivery.habsida.repository.ProductModifierGroupRepository;
import com.delivery.habsida.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductModifierGroupService {

    private final ProductModifierGroupRepository productModifierGroupRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final ProductRepository productRepository;

    public ProductModifierGroupService(ProductModifierGroupRepository productModifierGroupRepository,
                                       ModifierGroupRepository modifierGroupRepository,
                                       ProductRepository productRepository) {
        this.productModifierGroupRepository = productModifierGroupRepository;
        this.modifierGroupRepository = modifierGroupRepository;
        this.productRepository = productRepository;
    }

    private ModifierGroup getModifierGroupOrThrow(Long storeId, Long groupId) {
        return modifierGroupRepository.findByIdAndStoreId(groupId, storeId)
                .orElseThrow(() -> new ModifierGroupNotFoundException("Modifier group not found"));
    }

    private Product getProductOrThrow(Long storeId, Long productId) {
        return productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    private void checkNotDuplicate(Long productId, Long modifierGroupId) {
        if (productModifierGroupRepository.existsByProductIdAndModifierGroupId(productId, modifierGroupId)) {
            throw new ProductModifierGroupAlreadyExistsException("This modifier group is already created at this product");
        }
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ProductModifierGroupResponse createProductModifierGroup(Long storeId, Long productId, Long groupId) {
        Product product = getProductOrThrow(storeId, productId);
        ModifierGroup modifierGroup = getModifierGroupOrThrow(storeId, groupId);
        checkNotDuplicate(productId, groupId);

        ProductModifierGroup created = new ProductModifierGroup();
        created.setProduct(product);
        created.setModifierGroup(modifierGroup);

        return ProductModifierGroupResponse.from(productModifierGroupRepository.save(created));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication,#storeId)")
    public Page<ModifierGroupWithOptionsResponse> getModifierGroupWithOptions(Long storeId, Long productId, Pageable pageable) {
        getProductOrThrow(storeId, productId);

        return modifierGroupRepository.findModifierGroupsByProductId(productId, pageable)
                .map(ModifierGroupWithOptionsResponse::from);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteProductModifierGroup(Long storeId, Long productId, Long groupId) {
        getProductOrThrow(storeId, productId);
        getModifierGroupOrThrow(storeId, groupId);

        ProductModifierGroup productModifierGroup = productModifierGroupRepository.findByProductIdAndModifierGroupId(productId, groupId)
                .orElseThrow(() -> new ProductModifierGroupNotFoundException("Product modifier group not found"));

        productModifierGroupRepository.delete(productModifierGroup);
    }
}
