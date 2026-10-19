package com.delivery.habsida.dto;

import com.delivery.habsida.entity.StoreBreak;

import java.time.LocalTime;

public record StoreBreakResponse(
        Long id,
        Long storeHourId,
        LocalTime startTime,
        LocalTime endTime
){

    public static StoreBreakResponse from(StoreBreak storeBreak) {
        return new StoreBreakResponse(
                storeBreak.getId(),
                storeBreak.getStoreHour().getId(),
                storeBreak.getStartTime(),
                storeBreak.getEndTime()
        );
    }
}
