package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreHourDto;
import com.delivery.habsida.dto.StoreHourRequest;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreHour;
import com.delivery.habsida.exception.StoreHourNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreHourRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StoreHourService {
    private final StoreHourRepository storeHourRepository;
    private final StoreRepository storeRepository;

    public StoreHourService(StoreHourRepository storeHourRepository, StoreRepository storeRepository) {
        this.storeHourRepository = storeHourRepository;
        this.storeRepository = storeRepository;
    }

    private StoreHour getStoreHourOrThrow(Long storeId, Long hourId) {
        return storeHourRepository.findByIdAndStoreId(hourId, storeId)
                .orElseThrow(() -> new StoreHourNotFoundException("Store hour not found"));
    }

    private void applyRequest(StoreHour storeHour, StoreHourRequest request) {
        storeHour.setDayOfWeek(request.dayOfWeek());
        if (request.dayOff()) {
            storeHour.setOpenTime(null);
            storeHour.setCloseTime(null);
            storeHour.setLastOrderCutoffTime(null);
        } else {
            storeHour.setOpenTime(request.openTime());
            storeHour.setCloseTime(request.closeTime());
            storeHour.setLastOrderCutoffTime(request.lastOrderCutoffTime());
        }
        storeHour.setDayOff(request.dayOff());
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreHourDto createStoreHour(Long storeId, StoreHourRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        StoreHour storeHour = new StoreHour();
        storeHour.setStore(store);

        applyRequest(storeHour, request);

        return StoreHourDto.from(storeHourRepository.save(storeHour));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public List<StoreHourDto> getStoreHours(Long storeId) {
        return storeHourRepository.findByStoreId(storeId)
                .stream()
                .map(StoreHourDto::from).toList();
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreHourDto getStoreHour(Long storeId, Long hourId) {
        StoreHour storeHour = getStoreHourOrThrow(storeId, hourId);
        return StoreHourDto.from(storeHour);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreHourDto updateStoreHour(Long storeId, Long hourId, StoreHourRequest request) {
        StoreHour storeHour = getStoreHourOrThrow(storeId, hourId);
        applyRequest(storeHour, request);
        return StoreHourDto.from(storeHourRepository.save(storeHour));
    }


    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteStoreHour(Long storeId, Long hourId) {
        StoreHour storeHour = getStoreHourOrThrow(storeId, hourId);
        storeHourRepository.delete(storeHour);
    }
}
