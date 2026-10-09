package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreDeliveryRestrictionRequest;
import com.delivery.habsida.dto.StoreDeliveryRestrictionResponse;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliveryRestriction;
import com.delivery.habsida.exception.StoreDeliveryRestrictionAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliveryRestrictionNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreDeliveryRestrictionRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreDeliveryRestrictionServiceTest {
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreDeliveryRestrictionRepository storeDeliveryRestrictionRepository;

    @InjectMocks
    private StoreDeliveryRestrictionService storeDeliveryRestrictionService;

    private Store store;
    private StoreDeliveryRestriction storeDeliveryRestriction;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        storeDeliveryRestriction = new StoreDeliveryRestriction();
        storeDeliveryRestriction.setId(5L);
        storeDeliveryRestriction.setRestrictedArea("Seoul");
        storeDeliveryRestriction.setStore(store);
    }

    private StoreDeliveryRestrictionRequest defaultRequest() {
        return new StoreDeliveryRestrictionRequest(
                "Seoul"
        );
    }

    @Test
    void createStoreDeliveryRestriction_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(defaultRequest().restrictedArea(), 1L)).thenReturn(false);
        when(storeDeliveryRestrictionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoreDeliveryRestrictionResponse response = storeDeliveryRestrictionService.createStoreDeliveryRestriction(1L, defaultRequest());

        assertEquals("Seoul", response.restrictedArea());

        verify(storeDeliveryRestrictionRepository).save(any());
    }

    @Test
    void createStoreDeliveryRestriction_shouldThrowException_whenRestrictedAreaAlreadyExists() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(defaultRequest().restrictedArea(), 1L)).thenReturn(true);

        assertThrows(StoreDeliveryRestrictionAlreadyExistsException.class,
                () -> storeDeliveryRestrictionService.createStoreDeliveryRestriction(1L, defaultRequest()));

        verify(storeDeliveryRestrictionRepository, never()).save(any());
    }

    @Test
    void getStoreDeliveryRestrictions_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findAllByStoreId(1L)).thenReturn(List.of(storeDeliveryRestriction));

        List<StoreDeliveryRestrictionResponse> responses = storeDeliveryRestrictionService.getStoreDeliveryRestrictions(1L);

        assertEquals(1, responses.size());
        assertEquals(5L, responses.get(0).id());
        assertEquals("Seoul", responses.get(0).restrictedArea());
    }

    @Test
    void getStoreDeliveryRestrictions_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> storeDeliveryRestrictionService.getStoreDeliveryRestrictions(1L));
    }

    @Test
    void getStoreDeliveryRestriction_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryRestriction));

        StoreDeliveryRestrictionResponse response = storeDeliveryRestrictionService.getStoreDeliveryRestriction(1L, 5L);

        assertEquals(1L, response.storeId());
        assertEquals(5L, response.id());
        assertEquals("Seoul", response.restrictedArea());
    }

    @Test
    void getStoreDeliveryRestriction_shouldThrowException_whenRestrictionNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreDeliveryRestrictionNotFoundException.class,
                () -> storeDeliveryRestrictionService.getStoreDeliveryRestriction(1L, 5L));
    }

    @Test
    void updateStoreDeliveryRestriction_success() {
        StoreDeliveryRestrictionRequest updatedRequest = new StoreDeliveryRestrictionRequest(
                "Ansan"
        );

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryRestriction));
        when(storeDeliveryRestrictionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(updatedRequest.restrictedArea(), 1L)).thenReturn(false);

        StoreDeliveryRestrictionResponse response = storeDeliveryRestrictionService.updateStoreDeliveryRestriction(1L, 5L, updatedRequest);

        assertEquals("Ansan", response.restrictedArea());

        verify(storeDeliveryRestrictionRepository).save(any());
        verify(storeDeliveryRestrictionRepository).existsByRestrictedAreaAndStoreId("Ansan", 1L);
    }

    @Test
    void updateStoreDeliveryRestriction_shouldThrowException_whenRestrictionAlreadyExists() {
        StoreDeliveryRestrictionRequest updatedRequest = new StoreDeliveryRestrictionRequest(
                "Ansan"
        );

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryRestriction));
        when(storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(updatedRequest.restrictedArea(), 1L)).thenReturn(true);

        assertThrows(StoreDeliveryRestrictionAlreadyExistsException.class,
                () -> storeDeliveryRestrictionService.updateStoreDeliveryRestriction(1L, 5L, updatedRequest));

        verify(storeDeliveryRestrictionRepository, never()).save(any());
    }

    @Test
    void updateStoreDeliveryRestriction_shouldNotCheckDuplicate_whenRestrictedAreaNotChanged() {
        StoreDeliveryRestrictionRequest updatedRequest = new StoreDeliveryRestrictionRequest(
                "Seoul"
        );

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryRestriction));
        when(storeDeliveryRestrictionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoreDeliveryRestrictionResponse response = storeDeliveryRestrictionService.updateStoreDeliveryRestriction(1L, 5L, updatedRequest);

        assertEquals("Seoul", response.restrictedArea());

        verify(storeDeliveryRestrictionRepository).save(any());
        verify(storeDeliveryRestrictionRepository, never()).existsByRestrictedAreaAndStoreId(any(), anyLong());
    }

    @Test
    void deleteStoreDeliveryRestriction_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryRestrictionRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryRestriction));

        storeDeliveryRestrictionService.deleteStoreDeliveryRestriction(1L, 5L);

        verify(storeDeliveryRestrictionRepository).delete(storeDeliveryRestriction);
    }

    @Test
    void deleteStoreDeliveryRestriction_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> storeDeliveryRestrictionService.deleteStoreDeliveryRestriction(1L, 5L));

        verify(storeDeliveryRestrictionRepository, never()).delete(any());
    }
}
