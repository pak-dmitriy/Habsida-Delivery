package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreDeliveryAreaRequest;
import com.delivery.habsida.dto.StoreDeliveryAreaResponse;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliveryArea;
import com.delivery.habsida.exception.StoreDeliveryAreaAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliveryAreaNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreDeliveryAreaRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StoreDeliveryAreaService {
    private final StoreRepository storeRepository;
    private final StoreDeliveryAreaRepository storeDeliveryAreaRepository;

    public StoreDeliveryAreaService(StoreRepository storeRepository,
                                    StoreDeliveryAreaRepository storeDeliveryAreaRepository) {
        this.storeRepository = storeRepository;
        this.storeDeliveryAreaRepository = storeDeliveryAreaRepository;
    }

    private Store getStoreOrThrow(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));
    }

    private StoreDeliveryArea getAreaOrThrow(Long areaId, Long storeId) {
        return storeDeliveryAreaRepository.findByIdAndStoreId(areaId, storeId)
                .orElseThrow(() -> new StoreDeliveryAreaNotFoundException("Area not found"));
    }

    @Transactional
    public StoreDeliveryAreaResponse createStoreDeliveryArea(Long storeId,
                                                             StoreDeliveryAreaRequest request) {
        Store store = getStoreOrThrow(storeId);

        if (storeDeliveryAreaRepository.existsByDistrictNameAndStoreId(request.districtName(), storeId)) {
            throw new StoreDeliveryAreaAlreadyExistsException("Area already exists");
        }

        StoreDeliveryArea area = new StoreDeliveryArea();
        area.setDistrictName(request.districtName());
        area.setDeliveryFee(request.deliveryFee());
        area.setStore(store);

        return StoreDeliveryAreaResponse.from(storeDeliveryAreaRepository.save(area));
    }

    public List<StoreDeliveryAreaResponse> getStoreDeliveryAreas(Long storeId) {
        getStoreOrThrow(storeId);

        return storeDeliveryAreaRepository.findAllByStoreId(storeId)
                .stream()
                .map(StoreDeliveryAreaResponse::from)
                .toList();
    }


    public StoreDeliveryAreaResponse getStoreDeliveryArea(Long storeId,
                                                          Long areaId) {
        getStoreOrThrow(storeId);
        StoreDeliveryArea area = getAreaOrThrow(areaId, storeId);

        return StoreDeliveryAreaResponse.from(area);
    }

    @Transactional
    public StoreDeliveryAreaResponse updateStoreDeliveryArea(Long storeId,
                                                             Long areaId,
                                                             StoreDeliveryAreaRequest request) {
        getStoreOrThrow(storeId);
        StoreDeliveryArea area = getAreaOrThrow(areaId, storeId);

        if (!request.districtName().equals(area.getDistrictName())
                && storeDeliveryAreaRepository.existsByDistrictNameAndStoreId(request.districtName(), storeId)) {
            throw new StoreDeliveryAreaAlreadyExistsException("Area already exists");
        }

        area.setDistrictName(request.districtName());
        area.setDeliveryFee(request.deliveryFee());

        return StoreDeliveryAreaResponse.from(storeDeliveryAreaRepository.save(area));
    }

    @Transactional
    public void deleteStoreDeliveryArea(Long storeId, Long areaId) {
        getStoreOrThrow(storeId);
        StoreDeliveryArea area = getAreaOrThrow(areaId, storeId);

        storeDeliveryAreaRepository.delete(area);
    }
}
