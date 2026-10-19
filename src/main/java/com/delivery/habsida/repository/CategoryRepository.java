package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByStoreId(Long storeId, Pageable pageable);
}
