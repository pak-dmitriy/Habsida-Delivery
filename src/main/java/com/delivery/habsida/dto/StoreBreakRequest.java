package com.delivery.habsida.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record StoreBreakRequest (

        @NotNull(message = "Start time can not be empty")
        LocalTime startTime,

        @NotNull(message = "End time can not be empty")
        LocalTime endTime
){
    @JsonIgnore
    @AssertTrue(message = "Start time must be before end time")
    public boolean isStartTimeValid() {
       if (startTime == null || endTime == null) {
           return true;
       }

       return startTime.isBefore(endTime);
    }
}
