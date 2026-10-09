package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreDeliverySettingRequest;
import com.delivery.habsida.dto.StoreDeliverySettingResponse;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliverySetting;
import com.delivery.habsida.exception.StoreDeliverySettingAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliverySettingNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreDeliverySettingRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StoreDeliverySettingService {
    private final StoreRepository storeRepository;
    private final StoreDeliverySettingRepository storeDeliverySettingRepository;

    public StoreDeliverySettingService(StoreRepository storeRepository,
                                       StoreDeliverySettingRepository storeDeliverySettingRepository) {
        this.storeRepository = storeRepository;
        this.storeDeliverySettingRepository = storeDeliverySettingRepository;
    }

    private Store getStoreOrThrow(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));
    }

    private StoreDeliverySetting getSettingOrThrow(Long storeId) {
        return storeDeliverySettingRepository.findByStoreId(storeId)
                .orElseThrow(() -> new StoreDeliverySettingNotFoundException("Setting not found"));
    }

    @Transactional
    public StoreDeliverySettingResponse createStoreDeliverySetting(Long storeId,
                                                                   StoreDeliverySettingRequest request) {
        Store store = getStoreOrThrow(storeId);

        if (storeDeliverySettingRepository.existsByStoreId(storeId)) {
            throw new StoreDeliverySettingAlreadyExistsException("Setting already exists");
        }

        StoreDeliverySetting storeDeliverySetting = new StoreDeliverySetting();
        storeDeliverySetting.setStore(store);
        storeDeliverySetting.setDeliveryEnabled(request.deliveryEnabled());
        storeDeliverySetting.setDeliveryType(request.deliveryType());
        storeDeliverySetting.setMinOrderPrice(request.minOrderPrice());
        storeDeliverySetting.setFreeDeliveryThreshold(request.freeDeliveryThreshold());
        storeDeliverySetting.setMaxDistance(request.maxDistance());
        storeDeliverySetting.setDeliveryGeography(request.deliveryGeography());

        return StoreDeliverySettingResponse.from(storeDeliverySettingRepository.save(storeDeliverySetting));
    }

    public StoreDeliverySettingResponse getStoreDeliverySetting(Long storeId) {
        getStoreOrThrow(storeId);

        StoreDeliverySetting storeDeliverySetting = getSettingOrThrow(storeId);

        return StoreDeliverySettingResponse.from(storeDeliverySetting);
    }

    @Transactional
    public StoreDeliverySettingResponse updateStoreDeliverySetting(Long storeId,
                                                                   StoreDeliverySettingRequest request) {
        getStoreOrThrow(storeId);
        StoreDeliverySetting storeDeliverySetting = getSettingOrThrow(storeId);

        storeDeliverySetting.setDeliveryEnabled(request.deliveryEnabled());
        storeDeliverySetting.setDeliveryType(request.deliveryType());
        storeDeliverySetting.setMinOrderPrice(request.minOrderPrice());
        storeDeliverySetting.setFreeDeliveryThreshold(request.freeDeliveryThreshold());
        storeDeliverySetting.setMaxDistance(request.maxDistance());
        storeDeliverySetting.setDeliveryGeography(request.deliveryGeography());

        return StoreDeliverySettingResponse.from(storeDeliverySettingRepository.save(storeDeliverySetting));
    }
}
