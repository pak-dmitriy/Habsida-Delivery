package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreHourDto;
import com.delivery.habsida.dto.StoreHourRequest;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreBreak;
import com.delivery.habsida.entity.StoreHour;
import com.delivery.habsida.exception.InvalidStoreBreakException;
import com.delivery.habsida.exception.StoreHourNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreBreakRepository;
import com.delivery.habsida.repository.StoreHourRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreHourServiceTest {
    @Mock
    private StoreHourRepository storeHourRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreBreakRepository storeBreakRepository;

    @InjectMocks
    private StoreHourService storeHourService;

    private Store store;
    private StoreHour storeHour;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        storeHour = new StoreHour();
        storeHour.setId(1L);
        storeHour.setStore(store);
        storeHour.setDayOfWeek(DayOfWeek.MONDAY);
        storeHour.setOpenTime(LocalTime.of(9, 0));
        storeHour.setCloseTime(LocalTime.of(22, 0));
        storeHour.setDayOff(false);
        storeHour.setLastOrderCutoffTime(LocalTime.of(21, 30));
    }

    @Test
    void createStoreHour_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeHourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.TUESDAY,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0),
                false,
                LocalTime.of(19, 30)
        );

        StoreHourDto response = storeHourService.createStoreHour(1L, request);
        assertEquals(1L, response.storeId());
        assertEquals(DayOfWeek.TUESDAY, response.dayOfWeek());
        assertEquals(LocalTime.of(10, 0), response.openTime());
        assertEquals(LocalTime.of(20, 0), response.closeTime());
        assertEquals(LocalTime.of(19, 30), response.lastOrderCutoffTime());
        assertFalse(response.dayOff());
    }

    @Test
    void createStoreHour_dayOff_clearsTimes() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeHourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.SUNDAY,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0),
                true,
                LocalTime.of(19, 30)
        );

        StoreHourDto response = storeHourService.createStoreHour(1L, request);
        assertTrue(response.dayOff());
        assertNull(response.openTime());
        assertNull(response.closeTime());
        assertNull(response.lastOrderCutoffTime());
    }

    @Test
    void createStoreHour_storeNotFound_throwsException() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                false,
                LocalTime.of(21, 30)
        );

        assertThrows(StoreNotFoundException.class,
                () -> storeHourService.createStoreHour(1L, request));
        verify(storeHourRepository, never()).save(any());
    }

    @Test
    void getStoreHours_success() {
        when(storeHourRepository.findByStoreId(1L)).thenReturn(List.of(storeHour));

        List<StoreHourDto> response = storeHourService.getStoreHours(1L);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
    }

    @Test
    void getStoreHours_returnsEmptyList_whenNoHours() {
        when(storeHourRepository.findByStoreId(1L)).thenReturn(Collections.emptyList());

        List<StoreHourDto> response = storeHourService.getStoreHours(1L);
        assertTrue(response.isEmpty());
    }

    @Test
    void getStoreHour_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));

        StoreHourDto response = storeHourService.getStoreHour(1L, 1L);
        assertEquals(1L, response.id());
        assertEquals(DayOfWeek.MONDAY, response.dayOfWeek());
    }

    @Test
    void getStoreHour_notFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreHourNotFoundException.class,
                () -> storeHourService.getStoreHour(1L, 1L));
    }

    @Test
    void updateStoreHour_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeHourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.FRIDAY,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0),
                false,
                LocalTime.of(22, 30)
        );

        StoreHourDto response = storeHourService.updateStoreHour(1L, 1L, request);
        assertEquals(DayOfWeek.FRIDAY, response.dayOfWeek());
        assertEquals(LocalTime.of(11, 0), response.openTime());
        assertEquals(LocalTime.of(23, 0), response.closeTime());
        assertEquals(LocalTime.of(22, 30), response.lastOrderCutoffTime());
    }

    @Test
    void updateStoreHour_breakOutsideNewHours_throwsException() {
        StoreBreak storeBreak = new StoreBreak();
        storeBreak.setStartTime(LocalTime.of(13, 0));
        storeBreak.setEndTime(LocalTime.of(14, 0));

        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(1L)).thenReturn(List.of(storeBreak));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), false, null);

        assertThrows(InvalidStoreBreakException.class,
                () -> storeHourService.updateStoreHour(1L, 1L, request));
        verify(storeHourRepository, never()).save(any());
    }

    @Test
    void updateStoreHour_dayOffWithBreaks_throwsException() {
        StoreBreak storeBreak = new StoreBreak();
        storeBreak.setStartTime(LocalTime.of(13, 0));
        storeBreak.setEndTime(LocalTime.of(14, 0));

        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(1L)).thenReturn(List.of(storeBreak));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, null, null, true, null);

        assertThrows(InvalidStoreBreakException.class,
                () -> storeHourService.updateStoreHour(1L, 1L, request));
        verify(storeHourRepository, never()).save(any());
    }

    @Test
    void updateStoreHour_breakFitsNewHours_success() {
        StoreBreak storeBreak = new StoreBreak();
        storeBreak.setStartTime(LocalTime.of(13, 0));
        storeBreak.setEndTime(LocalTime.of(14, 0));

        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(1L)).thenReturn(List.of(storeBreak));
        when(storeHourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(20, 0), false, null);

        assertDoesNotThrow(() -> storeHourService.updateStoreHour(1L, 1L, request));
    }

    @Test
    void updateStoreHour_dayOff_clearsTimes() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeHourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY,
                null,
                null,
                true,
                null
        );

        StoreHourDto response = storeHourService.updateStoreHour(1L, 1L, request);
        assertTrue(response.dayOff());
        assertNull(response.openTime());
        assertNull(response.closeTime());
        assertNull(response.lastOrderCutoffTime());
    }

    @Test
    void updateStoreHour_notFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        StoreHourRequest request = new StoreHourRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                false,
                LocalTime.of(21, 30)
        );

        assertThrows(StoreHourNotFoundException.class,
                () -> storeHourService.updateStoreHour(1L, 1L, request));
        verify(storeHourRepository, never()).save(any());
    }

    @Test
    void deleteStoreHour_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));

        storeHourService.deleteStoreHour(1L, 1L);
        verify(storeHourRepository).delete(storeHour);
    }

    @Test
    void deleteStoreHour_notFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreHourNotFoundException.class,
                () -> storeHourService.deleteStoreHour(1L, 1L));
        verify(storeHourRepository, never()).delete(any());
    }
}