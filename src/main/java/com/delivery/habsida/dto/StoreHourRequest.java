package com.delivery.habsida.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record StoreHourRequest(

        @NotNull(message = "Day of week can not be empty")
        DayOfWeek dayOfWeek,

        LocalTime openTime,

        LocalTime closeTime,

        @NotNull(message = "Day off can not be empty")
        Boolean dayOff,

        LocalTime lastOrderCutoffTime
) {
    @JsonIgnore
    @AssertTrue(message = "Working day needs openTime and closeTime, and openTime must be before closeTime")
    public boolean isOpenTimeValid() {
        if (Boolean.TRUE.equals(dayOff)) {
            return true;
        }
        if (openTime == null || closeTime == null) {
            return false;
        }
        return openTime.isBefore(closeTime);
    }

    @JsonIgnore
    @AssertTrue(message = "Last order cutoff time must be within working hours")
    public boolean isLastOrderCutoffTimeValid() {
        if (Boolean.TRUE.equals(dayOff) || lastOrderCutoffTime == null) {
            return true;
        }

        if (openTime == null || closeTime == null) {
            return true;
        }
        return !lastOrderCutoffTime.isBefore(openTime)
                && !lastOrderCutoffTime.isAfter(closeTime);
    }
}
