package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliveryRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreDeliveryRestrictionRepository extends JpaRepository<StoreDeliveryRestriction, Long> {
    List<StoreDeliveryRestriction> findAllByStoreId(Long storeId);

    Optional<StoreDeliveryRestriction> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByRestrictedAreaAndStoreId(String restrictedArea, Long storeId);
}
