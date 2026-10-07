package com.delivery.habsida.repository;

import com.delivery.habsida.entity.StoreHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreHourRepository extends JpaRepository<StoreHour, Long> {
    List<StoreHour> findByStoreId(Long storeId);
    Optional<StoreHour> findByIdAndStoreId(Long id, Long storeId);
}
