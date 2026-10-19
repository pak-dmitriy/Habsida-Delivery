package com.delivery.habsida.repository;

import com.delivery.habsida.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Page<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId, Pageable pageable);
}
