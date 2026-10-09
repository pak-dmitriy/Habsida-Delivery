package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliveryArea;
import com.delivery.habsida.exception.StoreDeliveryAreaNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreDeliveryAreaRepository extends JpaRepository<StoreDeliveryArea, Long> {
    List<StoreDeliveryArea> findAllByStoreId(Long storeId);

    Optional<StoreDeliveryArea> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByDistrictNameAndStoreId(String districtName, Long storeId);

    Optional<StoreDeliveryArea> findByDistrictNameAndStoreId(String districtName, Long storeId);
}
