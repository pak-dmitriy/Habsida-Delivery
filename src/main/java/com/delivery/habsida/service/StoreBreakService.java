package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreBreakDto;
import com.delivery.habsida.dto.StoreBreakRequest;
import com.delivery.habsida.entity.StoreBreak;
import com.delivery.habsida.entity.StoreHour;
import com.delivery.habsida.exception.InvalidStoreBreakException;
import com.delivery.habsida.exception.StoreBreakNotFoundException;
import com.delivery.habsida.exception.StoreHourNotFoundException;
import com.delivery.habsida.repository.StoreBreakRepository;
import com.delivery.habsida.repository.StoreHourRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StoreBreakService {
    private final StoreHourRepository storeHourRepository;
    private final StoreBreakRepository storeBreakRepository;

    public StoreBreakService(StoreHourRepository storeHourRepository, StoreBreakRepository storeBreakRepository) {
        this.storeHourRepository = storeHourRepository;
        this.storeBreakRepository = storeBreakRepository;
    }

    private StoreHour getStoreHourOrThrow(Long storeId, Long hourId) {
        return storeHourRepository.findByIdAndStoreId(hourId, storeId)
                .orElseThrow(() -> new StoreHourNotFoundException("Store hour not found"));
    }

    private StoreBreak getStoreBreakOrThrow(Long storeId, Long hourId, Long breakId) {
        getStoreHourOrThrow(storeId, hourId);
        return storeBreakRepository.findByIdAndStoreHourId(breakId, hourId)
                .orElseThrow(() -> new StoreBreakNotFoundException("Break not found"));
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreBreakDto createStoreBreak(Long storeId, Long hourId, StoreBreakRequest request) {
        StoreHour storeHour = getStoreHourOrThrow(storeId, hourId);

        if (storeHour.isDayOff()) {
            throw new InvalidStoreBreakException("Cannot create a break for a day off");
        }
        if (request.startTime() == null || request.endTime() == null || !request.startTime().isBefore(request.endTime())) {
            throw new InvalidStoreBreakException("Invalid start time or end time");
        }

        if (storeHour.getOpenTime() == null || storeHour.getCloseTime() == null
                || request.startTime().isBefore(storeHour.getOpenTime())
                || request.endTime().isAfter(storeHour.getCloseTime())) {
            throw new InvalidStoreBreakException("Invalid start time or end time");
        }

        if (storeBreakRepository.existsByStoreHourIdAndStartTimeLessThanAndEndTimeGreaterThan(hourId, request.endTime(), request.startTime())) {
            throw new InvalidStoreBreakException("Break overlaps with an existing break");
        }

        StoreBreak storeBreak = new StoreBreak();
        storeBreak.setStoreHour(storeHour);
        storeBreak.setStartTime(request.startTime());
        storeBreak.setEndTime(request.endTime());
        return StoreBreakDto.from(storeBreakRepository.save(storeBreak));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public List<StoreBreakDto> getStoreBreaks(Long storeId, Long hourId) {
        getStoreHourOrThrow(storeId, hourId);
        return storeBreakRepository.findByStoreHourId(hourId)
                .stream()
                .map(StoreBreakDto::from).toList();
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreBreakDto getStoreBreak(Long storeId, Long hourId, Long breakId) {
        StoreBreak storeBreak = getStoreBreakOrThrow(storeId, hourId, breakId);
        return StoreBreakDto.from(storeBreak);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreBreakDto updateStoreBreak(Long storeId, Long hourId, Long breakId, StoreBreakRequest request) {
        StoreBreak storeBreak = getStoreBreakOrThrow(storeId, hourId, breakId);
        StoreHour storeHour = storeBreak.getStoreHour();

        if (storeHour.isDayOff()) {
            throw new InvalidStoreBreakException("Cannot update a break for a day off");
        }
        if (request.startTime() == null || request.endTime() == null || !request.startTime().isBefore(request.endTime())) {
            throw new InvalidStoreBreakException("Invalid start time or end time");
        }

        if (storeHour.getOpenTime() == null || storeHour.getCloseTime() == null
                || request.startTime().isBefore(storeHour.getOpenTime())
                || request.endTime().isAfter(storeHour.getCloseTime())) {
            throw new InvalidStoreBreakException("Invalid start time or end time");
        }

        if (storeBreakRepository.existsByStoreHourIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(hourId, breakId, request.endTime(), request.startTime())) {
            throw new InvalidStoreBreakException("Break overlaps with an existing break");
        }

        storeBreak.setStartTime(request.startTime());
        storeBreak.setEndTime(request.endTime());
        return StoreBreakDto.from(storeBreakRepository.save(storeBreak));
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteStoreBreak(Long storeId, Long hourId, Long breakId) {
        StoreBreak storeBreak = getStoreBreakOrThrow(storeId, hourId, breakId);
        storeBreakRepository.delete(storeBreak);
    }

}
