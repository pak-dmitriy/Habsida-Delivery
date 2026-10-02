package com.delivery.habsida.dto;

import com.delivery.habsida.entity.StoreHour;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record StoreHourDto (
     Long id,
     Long storeId,
     DayOfWeek dayOfWeek,
     LocalTime openTime,
     LocalTime closeTime,
     boolean dayOff,
     LocalTime lastOrderCutoffTime
){

    public static StoreHourDto from(StoreHour storeHour) {
        return new StoreHourDto(
                storeHour.getId(),
                storeHour.getStore().getId(),
                storeHour.getDayOfWeek(),
                storeHour.getOpenTime(),
                storeHour.getCloseTime(),
                storeHour.isDayOff(),
                storeHour.getLastOrderCutoffTime()
        );
    }
}
