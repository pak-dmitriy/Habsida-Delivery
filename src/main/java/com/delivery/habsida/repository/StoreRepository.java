package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    @Modifying
    @Query(value = "UPDATE store SET last_order_number = last_order_number + 1 WHERE id = :storeId RETURNING last_order_number",
            nativeQuery = true)
    List<Long> incrementAndGetOrderNumber(@Param("storeId") Long storeId);

    @Query("select s from Store s where exists (select 1 from UserStoreAccess a where a.store = s and a.user.id = :userId)")
    Page<Store> findStoresByUserId(@Param("userId")Long userId, Pageable pageable);
}
