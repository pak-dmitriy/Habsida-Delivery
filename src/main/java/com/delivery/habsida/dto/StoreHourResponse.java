package com.delivery.habsida.dto;

import com.delivery.habsida.entity.StoreHour;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record StoreHourResponse(
     Long id,
     Long storeId,
     DayOfWeek dayOfWeek,
     LocalTime openTime,
     LocalTime closeTime,
     boolean dayOff,
     LocalTime lastOrderCutoffTime
){

    public static StoreHourResponse from(StoreHour storeHour) {
        return new StoreHourResponse(
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
