package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreBreakRequest;
import com.delivery.habsida.dto.StoreBreakResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreBreakServiceTest {
    @Mock
    private StoreHourRepository storeHourRepository;

    @Mock
    private StoreBreakRepository storeBreakRepository;

    @InjectMocks
    private StoreBreakService storeBreakService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private StoreHour storeHour;

    @BeforeEach
    void setUp() {
        Store store = new Store();
        store.setId(1L);

        storeHour = new StoreHour();
        storeHour.setId(2L);
        storeHour.setOpenTime(LocalTime.of(9, 0));
        storeHour.setCloseTime(LocalTime.of(18, 0));
    }

    private StoreBreak defaultBreak() {
        StoreBreak storeBreak = new StoreBreak();
        storeBreak.setId(5L);
        storeBreak.setStoreHour(storeHour);
        storeBreak.setStartTime(LocalTime.of(12, 0));
        storeBreak.setEndTime(LocalTime.of(13, 0));

        return storeBreak;
    }

    private StoreBreakRequest defaultRequest() {
        return new StoreBreakRequest(
                LocalTime.of(12, 0),
                LocalTime.of(13, 0)
        );
    }

    @Test
    void createStoreBreak_success() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreBreakResponse response = storeBreakService.createStoreBreak(1L, 2L, defaultRequest());

        assertEquals(2L, response.storeHourId());
        assertEquals(LocalTime.of(12, 0), response.startTime());
        assertEquals(LocalTime.of(13, 0), response.endTime());

        verify(storeHourRepository).findByIdAndStoreId(2L, 1L);
        verify(storeBreakRepository).save(any());
    }

    @Test
    void createStoreBreak_overlapsExistingBreak_throwsException() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.existsByStoreHourIdAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), any(), any())).thenReturn(true);

        InvalidStoreBreakException exception = assertThrows(InvalidStoreBreakException.class,
                () -> storeBreakService.createStoreBreak(1L, 2L, defaultRequest()));
        assertEquals("Break overlaps with an existing break", exception.getMessage());

        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void createStoreBreak_throwsException_whenStoreHourNotFound() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreHourNotFoundException.class,
                () -> storeBreakService.createStoreBreak(1L, 2L, defaultRequest()));

        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void getStoreBreaks_success() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(2L, pageable)).thenReturn(new PageImpl<>(List.of(defaultBreak())));

        Page<StoreBreakResponse> page = storeBreakService.getStoreBreaks(1L, 2L, pageable);
        StoreBreakResponse response = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(5L, response.id());
        assertEquals(LocalTime.of(12, 0), response.startTime());
        assertEquals(LocalTime.of(13, 0), response.endTime());
        assertEquals(2L, response.storeHourId());

        verify(storeBreakRepository).findByStoreHourId(2L, pageable);
    }

    @Test
    void getStoreBreaks_returnsEmptyList_whenNoBreaks() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByStoreHourId(2L, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<StoreBreakResponse> page = storeBreakService.getStoreBreaks(1L, 2L, pageable);

        assertTrue(page.isEmpty());

        verify(storeBreakRepository).findByStoreHourId(2L, pageable);
    }

    @Test
    void getStoreBreaks_throwsException_hourNotFound() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreHourNotFoundException.class,
                () -> storeBreakService.getStoreBreaks(1L, 2L, pageable));
        ;
        verify(storeHourRepository).findByIdAndStoreId(2L, 1L);
        verify(storeBreakRepository, never()).findByStoreHourId(any(), any());
    }

    @Test
    void getStoreBreak_success() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.of(defaultBreak()));

        StoreBreakResponse response = storeBreakService.getStoreBreak(1L, 2L, 5L);

        assertEquals(5L, response.id());
        assertEquals(LocalTime.of(12, 0), response.startTime());
        assertEquals(LocalTime.of(13, 0), response.endTime());

        verify(storeHourRepository).findByIdAndStoreId(2L, 1L);
        verify(storeBreakRepository).findByIdAndStoreHourId(5L, 2L);
    }

    @Test
    void getStoreBreak_throwsException_notFound() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(StoreBreakNotFoundException.class,
                () -> storeBreakService.getStoreBreak(1L, 2L, 5L));

        verify(storeHourRepository).findByIdAndStoreId(2L, 1L);
        verify(storeBreakRepository).findByIdAndStoreHourId(5L, 2L);
    }

    @Test
    void updateStoreBreak_success() {
        StoreBreakRequest request = new StoreBreakRequest(
                LocalTime.of(15, 0),
                LocalTime.of(16, 0)
        );

        StoreBreak storeBreak = defaultBreak();

        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.of(storeBreak));
        when(storeBreakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreBreakResponse response = storeBreakService.updateStoreBreak(1L, 2L, 5L, request);

        assertEquals(LocalTime.of(15, 0), response.startTime());
        assertEquals(LocalTime.of(16, 0), response.endTime());

        verify(storeHourRepository).findByIdAndStoreId(2L, 1L);
        verify(storeBreakRepository).findByIdAndStoreHourId(5L, 2L);
        verify(storeBreakRepository).save(storeBreak);
    }

    @Test
    void updateStoreBreak_throwsException_overlapsExistingBreak() {
        StoreBreakRequest request = new StoreBreakRequest(
                LocalTime.of(12, 0),
                LocalTime.of(12, 30));

        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.of(defaultBreak()));
        when(storeBreakRepository.existsByStoreHourIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), anyLong(), any(), any())).thenReturn(true);

        InvalidStoreBreakException exception = assertThrows(InvalidStoreBreakException.class,
                () -> storeBreakService.updateStoreBreak(1L, 2L, 5L, request));
        assertEquals("Break overlaps with an existing break", exception.getMessage());

        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void updateStoreBreak_throwsException_breakNotFound() {
        StoreBreakRequest request = new StoreBreakRequest(
                LocalTime.of(15, 0),
                LocalTime.of(16, 0)
        );

        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(StoreBreakNotFoundException.class,
                () -> storeBreakService.updateStoreBreak(1L, 2L, 5L, request));

        verify(storeBreakRepository, never()).save(any());
    }

    @Test
    void deleteStoreBreak_success() {
        StoreBreak storeBreak = defaultBreak();

        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.of(storeBreak));

        storeBreakService.deleteStoreBreak(1L, 2L, 5L);

        verify(storeBreakRepository).delete(storeBreak);
    }

    @Test
    void deleteStoreBreak_throwsException_breakNotFound() {
        when(storeHourRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(storeHour));
        when(storeBreakRepository.findByIdAndStoreHourId(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(StoreBreakNotFoundException.class,
                () -> storeBreakService.deleteStoreBreak(1L, 2L, 5L));

        verify(storeBreakRepository, never()).delete(any());
    }
}