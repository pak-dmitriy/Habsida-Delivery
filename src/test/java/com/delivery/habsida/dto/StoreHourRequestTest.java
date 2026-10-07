package com.delivery.habsida.dto;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreHourRequestTest {

    @Test
    void lastOrderCutoffTime_afterCloseTime_isInvalid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), false, LocalTime.of(23, 0));

        assertFalse(request.isLastOrderCutoffTimeValid());
    }

    @Test
    void lastOrderCutoffTime_beforeOpenTime_isInvalid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), false, LocalTime.of(8, 0));

        assertFalse(request.isLastOrderCutoffTimeValid());
    }

    @Test
    void lastOrderCutoffTime_withinWorkingHours_isValid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), false, LocalTime.of(16, 30));

        assertTrue(request.isLastOrderCutoffTimeValid());
    }

    @Test
    void openTime_afterCloseTime_isInvalid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(9, 0), false, null);
        assertFalse(request.isOpenTimeValid());
    }
    @Test
    void workingDay_withoutOpenTime_isInvalid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, null, LocalTime.of(17, 0), false, null);
        assertFalse(request.isOpenTimeValid());
    }

    @Test
    void dayOff_withoutTimes_isValid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, null, null, true, null);
        assertTrue(request.isOpenTimeValid());
    }
    @Test
    void workingDay_withCorrectTimes_isValid() {
        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), true, null);
        assertTrue(request.isOpenTimeValid());
    }
}