package com.delivery.habsida.repository;

import com.delivery.habsida.entity.ProductModifierGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductModifierGroupRepository extends JpaRepository<ProductModifierGroup, Long> {
    Optional<ProductModifierGroup> findByProductIdAndModifierGroupId(Long productId, Long modifierGroupId);

    boolean existsByProductIdAndModifierGroupId(Long productId, Long modifierGroupId);

    Page<ProductModifierGroup> findByProductId(Long productId, Pageable pageable);

    List<ProductModifierGroup> findAllByProductId(Long productId);
}
