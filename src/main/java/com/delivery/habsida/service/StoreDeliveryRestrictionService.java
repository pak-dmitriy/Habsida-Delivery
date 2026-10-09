package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreDeliveryRestrictionRequest;
import com.delivery.habsida.dto.StoreDeliveryRestrictionResponse;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliveryRestriction;
import com.delivery.habsida.exception.StoreDeliveryRestrictionAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliveryRestrictionNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreDeliveryRestrictionRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StoreDeliveryRestrictionService {
    private final StoreRepository storeRepository;
    private final StoreDeliveryRestrictionRepository storeDeliveryRestrictionRepository;

    public StoreDeliveryRestrictionService(StoreDeliveryRestrictionRepository storeDeliveryRestrictionRepository,
                                           StoreRepository storeRepository) {
        this.storeDeliveryRestrictionRepository = storeDeliveryRestrictionRepository;
        this.storeRepository = storeRepository;
    }

    private Store getStoreOrThrow(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));
    }

    private StoreDeliveryRestriction getRestrictionOrThrow(Long restrictionId, Long storeId) {
        return storeDeliveryRestrictionRepository.findByIdAndStoreId(restrictionId, storeId)
                .orElseThrow(() -> new StoreDeliveryRestrictionNotFoundException("Restriction not found"));
    }

    @Transactional
    public StoreDeliveryRestrictionResponse createStoreDeliveryRestriction(Long storeId,
                                                                           StoreDeliveryRestrictionRequest request) {
        Store store = getStoreOrThrow(storeId);

        if (storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(request.restrictedArea(), storeId)) {
            throw new StoreDeliveryRestrictionAlreadyExistsException("Restricted area already exists");
        }

        StoreDeliveryRestriction restriction = new StoreDeliveryRestriction();
        restriction.setRestrictedArea(request.restrictedArea());
        restriction.setStore(store);

        return StoreDeliveryRestrictionResponse.from(storeDeliveryRestrictionRepository.save(restriction));
    }

    public List<StoreDeliveryRestrictionResponse> getStoreDeliveryRestrictions(Long storeId) {
        getStoreOrThrow(storeId);

        return storeDeliveryRestrictionRepository.findAllByStoreId(storeId)
                .stream()
                .map(StoreDeliveryRestrictionResponse::from)
                .toList();
    }

    public StoreDeliveryRestrictionResponse getStoreDeliveryRestriction(Long storeId, Long restrictionId) {
        getStoreOrThrow(storeId);
        StoreDeliveryRestriction restriction = getRestrictionOrThrow(restrictionId, storeId);

        return StoreDeliveryRestrictionResponse.from(restriction);
    }

    @Transactional
    public StoreDeliveryRestrictionResponse updateStoreDeliveryRestriction(Long storeId,
                                                                           Long restrictionId,
                                                                           StoreDeliveryRestrictionRequest request) {
        getStoreOrThrow(storeId);
        StoreDeliveryRestriction restriction = getRestrictionOrThrow(restrictionId, storeId);

        if (!request.restrictedArea().equals(restriction.getRestrictedArea())
                && storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(request.restrictedArea(), storeId)) {
            throw new StoreDeliveryRestrictionAlreadyExistsException("Restriction already exists");
        }
        restriction.setRestrictedArea(request.restrictedArea());

        return StoreDeliveryRestrictionResponse.from(storeDeliveryRestrictionRepository.save(restriction));
    }

    @Transactional
    public void deleteStoreDeliveryRestriction(Long storeId, Long restrictionId) {

        getStoreOrThrow(storeId);
        StoreDeliveryRestriction restriction = getRestrictionOrThrow(restrictionId, storeId);

        storeDeliveryRestrictionRepository.delete(restriction);
    }
}
