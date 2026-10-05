package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreBreakDto;
import com.delivery.habsida.dto.StoreBreakRequest;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreBreak;
import com.delivery.habsida.entity.StoreHour;
import com.delivery.habsida.exception.InvalidStoreBreakException;
import com.delivery.habsida.exception.StoreBreakNotFoundException;
import com.delivery.habsida.exception.StoreHourNotFoundException;
import com.delivery.habsida.repository.StoreBreakRepository;
import com.delivery.habsida.repository.StoreHourRepository;
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
public class StoreBreakServiceTest {
    @Mock
    private StoreHourRepository storeHourRepository;

    @Mock
    private StoreBreakRepository storeBreakRepository;

    @InjectMocks
    private StoreBreakService storeBreakService;

    private StoreHour storeHour;
    private StoreBreak storeBreak;

    @BeforeEach
    void setUp() {
        Store store = new Store();
        store.setId(1L);

        storeHour = new StoreHour();
        storeHour.setId(1L);
        storeHour.setStore(store);
        storeHour.setDayOfWeek(DayOfWeek.MONDAY);
        storeHour.setOpenTime(LocalTime.of(9, 0));
        storeHour.setCloseTime(LocalTime.of(18, 0));

        storeBreak = new StoreBreak();
        storeBreak.setId(1L);
        storeBreak.setStoreHour(storeHour);
        storeBreak.setStartTime(LocalTime.of(13, 0));
        storeBreak.setEndTime(LocalTime.of(14, 0));
    }

    @Test
    void createStoreBreak_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(15, 0), LocalTime.of(16, 0));

        StoreBreakDto response = storeBreakService.createStoreBreak(1L, 1L, request);
        assertEquals(1L, response.storeHourId());
        assertEquals(LocalTime.of(15, 0), response.startTime());
        assertEquals(LocalTime.of(16, 0), response.endTime());
    }

    @Test
    void createStoreBreak_overlapsExistingBreak_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.existsByStoreHourIdAndStartTimeLessThanAndEndTimeGreaterThan(
                1L, LocalTime.of(14, 30), LocalTime.of(13, 30))).thenReturn(true);

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(13, 30), LocalTime.of(14, 30));

        assertThrows(InvalidStoreBreakException.class,
                () -> storeBreakService.createStoreBreak(1L, 1L, request));
        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void createStoreBreak_hourNotFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(15, 0), LocalTime.of(16, 0));

        assertThrows(StoreHourNotFoundException.class,
                () -> storeBreakService.createStoreBreak(1L, 1L, request));
        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void createStoreBreak_hourOfAnotherStore_throwsException() {
        // день 1 принадлежит магазину 1, а запрос пришёл от магазина 2
        when(storeHourRepository.findByIdAndStoreId(1L, 2L)).thenReturn(Optional.empty());

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(15, 0), LocalTime.of(16, 0));

        assertThrows(StoreHourNotFoundException.class,
                () -> storeBreakService.createStoreBreak(2L, 1L, request));
        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void getStoreBreaks_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(1L)).thenReturn(List.of(storeBreak));

        List<StoreBreakDto> response = storeBreakService.getStoreBreaks(1L, 1L);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
    }

    @Test
    void getStoreBreaks_returnsEmptyList_whenNoBreaks() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(1L)).thenReturn(Collections.emptyList());

        List<StoreBreakDto> response = storeBreakService.getStoreBreaks(1L, 1L);
        assertTrue(response.isEmpty());
    }

    @Test
    void getStoreBreaks_hourNotFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreHourNotFoundException.class,
                () -> storeBreakService.getStoreBreaks(1L, 1L));
        verify(storeBreakRepository, never()).findByStoreHourId(any());
    }

    @Test
    void getStoreBreak_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.of(storeBreak));

        StoreBreakDto response = storeBreakService.getStoreBreak(1L, 1L, 1L);
        assertEquals(1L, response.id());
        assertEquals(LocalTime.of(13, 0), response.startTime());
        assertEquals(LocalTime.of(14, 0), response.endTime());
    }

    @Test
    void getStoreBreak_notFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreBreakNotFoundException.class,
                () -> storeBreakService.getStoreBreak(1L, 1L, 1L));
    }

    @Test
    void updateStoreBreak_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.of(storeBreak));
        when(storeBreakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(15, 0), LocalTime.of(16, 0));

        StoreBreakDto response = storeBreakService.updateStoreBreak(1L, 1L, 1L, request);
        assertEquals(LocalTime.of(15, 0), response.startTime());
        assertEquals(LocalTime.of(16, 0), response.endTime());
    }

    @Test
    void updateStoreBreak_overlapsExistingBreak_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.of(storeBreak));
        when(storeBreakRepository.existsByStoreHourIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                1L, 1L, LocalTime.of(16, 0), LocalTime.of(15, 0))).thenReturn(true);

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(15, 0), LocalTime.of(16, 0));

        assertThrows(InvalidStoreBreakException.class,
                () -> storeBreakService.updateStoreBreak(1L, 1L, 1L, request));
        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void updateStoreBreak_notFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.empty());

        StoreBreakRequest request = new StoreBreakRequest(LocalTime.of(15, 0), LocalTime.of(16, 0));

        assertThrows(StoreBreakNotFoundException.class,
                () -> storeBreakService.updateStoreBreak(1L, 1L, 1L, request));
        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void deleteStoreBreak_success() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.of(storeBreak));

        storeBreakService.deleteStoreBreak(1L, 1L, 1L);
        verify(storeBreakRepository).delete(storeBreak);
    }

    @Test
    void deleteStoreBreak_notFound_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreBreakNotFoundException.class,
                () -> storeBreakService.deleteStoreBreak(1L, 1L, 1L));
        verify(storeBreakRepository, never()).delete(any());
    }
}