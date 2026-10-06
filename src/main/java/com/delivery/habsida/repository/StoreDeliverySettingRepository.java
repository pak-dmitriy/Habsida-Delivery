package com.delivery.habsida.repository;

import com.delivery.habsida.entity.StoreDeliverySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreDeliverySettingRepository extends JpaRepository<StoreDeliverySetting, Long> {
    Optional<StoreDeliverySetting> findByStoreId(Long storeId);

    boolean existsByStoreId(Long storeId);
}
