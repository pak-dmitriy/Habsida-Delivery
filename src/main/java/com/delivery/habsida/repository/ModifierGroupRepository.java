package com.delivery.habsida.repository;

import com.delivery.habsida.entity.ModifierGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, Long> {
    Page<ModifierGroup> findByStoreId(Long storeId, Pageable pageable);
    Optional<ModifierGroup> findByIdAndStoreId(Long id, Long storeId);
}
