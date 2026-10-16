package com.delivery.habsida.repository;

import com.delivery.habsida.entity.ModifierGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, Long> {
    Page<ModifierGroup> findByStoreId(Long storeId, Pageable pageable);

    Optional<ModifierGroup> findByIdAndStoreId(Long id, Long storeId);

    @Query("select mg from ModifierGroup mg where exists (select 1 from ProductModifierGroup pmg where pmg.modifierGroup = mg and pmg.product.id = :productId)")
    Page<ModifierGroup> findModifierGroupsByProductId(@Param("productId") Long productId, Pageable pageable);
}
