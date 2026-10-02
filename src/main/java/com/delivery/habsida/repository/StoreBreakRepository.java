package com.delivery.habsida.repository;

import com.delivery.habsida.entity.StoreBreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreBreakRepository extends JpaRepository<StoreBreak, Long> {
    List<StoreBreak> findByStoreHourId(Long storeHourId);
    Optional<StoreBreak> findByIdAndStoreHourId(Long id, Long storeHourId);
}
