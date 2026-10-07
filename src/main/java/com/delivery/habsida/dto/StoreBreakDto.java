package com.delivery.habsida.dto;

import com.delivery.habsida.entity.StoreBreak;

import java.time.LocalTime;

public record StoreBreakDto (
        Long id,
        Long storeHourId,
        LocalTime startTime,
        LocalTime endTime
){

    public static StoreBreakDto from(StoreBreak storeBreak) {
        return new StoreBreakDto(
                storeBreak.getId(),
                storeBreak.getStoreHour().getId(),
                storeBreak.getStartTime(),
                storeBreak.getEndTime()
        );
    }
}
