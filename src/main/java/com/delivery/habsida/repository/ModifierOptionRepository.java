package com.delivery.habsida.repository;

import com.delivery.habsida.entity.ModifierOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModifierOptionRepository extends JpaRepository<ModifierOption, Long> {
    Optional<ModifierOption> findByIdAndModifierGroupId(Long id, Long modifierGroupId);
    Page<ModifierOption> findByModifierGroupIdOrderByIdAsc(Long modifierGroupId, Pageable pageable);
}
