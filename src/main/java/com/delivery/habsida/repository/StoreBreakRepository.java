package com.delivery.habsida.repository;

import com.delivery.habsida.entity.StoreBreak;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface StoreBreakRepository extends JpaRepository<StoreBreak, Long> {
    Page<StoreBreak> findByStoreHourId(Long storeHourId, Pageable pageable);

    Optional<StoreBreak> findByIdAndStoreHourId(Long id, Long storeHourId);

    boolean existsByStoreHourIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long storeHourId, LocalTime endTime, LocalTime startTime);

    boolean existsByStoreHourIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long storeHourId, Long id, LocalTime endTime, LocalTime startTime);

    List<StoreBreak> findAllByStoreHourId(Long storeHourId);
}
