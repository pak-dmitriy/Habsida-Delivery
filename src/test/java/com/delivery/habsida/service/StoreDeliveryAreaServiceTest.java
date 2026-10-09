package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreDeliveryAreaRequest;
import com.delivery.habsida.dto.StoreDeliveryAreaResponse;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliveryArea;
import com.delivery.habsida.exception.StoreDeliveryAreaAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliveryAreaNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreDeliveryAreaRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreDeliveryAreaServiceTest {
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreDeliveryAreaRepository storeDeliveryAreaRepository;

    @InjectMocks
    private StoreDeliveryAreaService storeDeliveryAreaService;

    private Store store;
    private StoreDeliveryArea storeDeliveryArea;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        storeDeliveryArea = new StoreDeliveryArea();
        storeDeliveryArea.setId(5L);
        storeDeliveryArea.setDistrictName("District");
        storeDeliveryArea.setDeliveryFee(new BigDecimal("10.00"));
        storeDeliveryArea.setStore(store);
    }

    private StoreDeliveryAreaRequest defaultRequest() {
        return new StoreDeliveryAreaRequest(
                "District",
                new BigDecimal("10.00")
        );
    }

    @Test
    void createStoreDeliveryArea_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.existsByDistrictNameAndStoreId("District", 1L)).thenReturn(false);
        when(storeDeliveryAreaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoreDeliveryAreaResponse response = storeDeliveryAreaService.createStoreDeliveryArea(1L, defaultRequest());

        verify(storeDeliveryAreaRepository).save(any());

        assertEquals("District", response.districtName());
        assertEquals(new BigDecimal("10.00"), response.deliveryFee());
    }

    @Test
    void createStoreDeliveryArea_shouldThrowException_whenAreaAlreadyExist() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.existsByDistrictNameAndStoreId("District", 1L)).thenReturn(true);

        assertThrows(StoreDeliveryAreaAlreadyExistsException.class,
                () -> storeDeliveryAreaService.createStoreDeliveryArea(1L, defaultRequest()));

        verify(storeDeliveryAreaRepository, never()).save(any());
    }

    @Test
    void getStoreDeliveryAreas_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.findAllByStoreId(1L)).thenReturn(List.of(storeDeliveryArea));

        List<StoreDeliveryAreaResponse> responses = storeDeliveryAreaService.getStoreDeliveryAreas(1L);

        assertEquals(1, responses.size());
        assertEquals(5L, responses.get(0).id());
    }

    @Test
    void getStoreDeliveryAreas_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> storeDeliveryAreaService.getStoreDeliveryAreas(1L));

        verify(storeDeliveryAreaRepository, never()).findAllByStoreId(anyLong());
    }

    @Test
    void getStoreDeliveryArea_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryArea));

        StoreDeliveryAreaResponse response = storeDeliveryAreaService.getStoreDeliveryArea(1L, 5L);

        assertEquals(5L, response.id());
        assertEquals("District", response.districtName());
        assertEquals(new BigDecimal("10.00"), response.deliveryFee());
    }

    @Test
    void getStoreDeliveryArea_shouldThrowException_whenAreaNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(StoreDeliveryAreaNotFoundException.class,
                () -> storeDeliveryAreaService.getStoreDeliveryArea(1L, 5L));
    }

    @Test
    void updateStoreDeliveryArea_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryArea));
        when(storeDeliveryAreaRepository.existsByDistrictNameAndStoreId("District1", 1L)).thenReturn(false);
        when(storeDeliveryAreaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoreDeliveryAreaRequest updatedRequest = new StoreDeliveryAreaRequest(
                "District1",
                new BigDecimal("20.00")
        );
        StoreDeliveryAreaResponse response = storeDeliveryAreaService.updateStoreDeliveryArea(1L, 5L, updatedRequest);

        assertEquals("District1", response.districtName());
        assertEquals(new BigDecimal("20.00"), response.deliveryFee());

        verify(storeDeliveryAreaRepository).save(any());
        verify(storeDeliveryAreaRepository).existsByDistrictNameAndStoreId("District1", 1L);
    }

    @Test
    void updateStoreDeliveryArea_shouldThrowException_whenAreaAlreadyExist() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryArea));
        when(storeDeliveryAreaRepository.existsByDistrictNameAndStoreId("District1", 1L)).thenReturn(true);

        StoreDeliveryAreaRequest updatedRequest = new StoreDeliveryAreaRequest(
                "District1",
                new BigDecimal("10.00")
        );

        assertThrows(StoreDeliveryAreaAlreadyExistsException.class,
                () -> storeDeliveryAreaService.updateStoreDeliveryArea(1L, 5L, updatedRequest));

        verify(storeDeliveryAreaRepository, never()).save(any());
    }

    @Test
    void deleteStoreDeliveryArea_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliveryAreaRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(storeDeliveryArea));

        storeDeliveryAreaService.deleteStoreDeliveryArea(1L, 5L);

        verify(storeDeliveryAreaRepository).delete(storeDeliveryArea);
    }

    @Test
    void deleteStoreDeliveryArea_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> storeDeliveryAreaService.deleteStoreDeliveryArea(1L, 5L));

        verify(storeDeliveryAreaRepository, never()).delete(any());
    }
}
