package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreDeliverySettingRequest;
import com.delivery.habsida.dto.StoreDeliverySettingResponse;
import com.delivery.habsida.entity.DeliveryGeography;
import com.delivery.habsida.entity.DeliveryType;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.StoreDeliverySetting;
import com.delivery.habsida.exception.StoreDeliverySettingAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliverySettingNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreDeliverySettingRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreDeliverySettingServiceTest {
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreDeliverySettingRepository storeDeliverySettingRepository;

    @InjectMocks
    private StoreDeliverySettingService storeDeliverySettingService;

    private Store store;
    private StoreDeliverySetting storeDeliverySetting;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        storeDeliverySetting = new StoreDeliverySetting();
        storeDeliverySetting.setId(1L);
        storeDeliverySetting.setDeliveryEnabled(true);
        storeDeliverySetting.setDeliveryType(DeliveryType.SAME_DAY);
        storeDeliverySetting.setMinOrderPrice(new BigDecimal("10.00"));
        storeDeliverySetting.setFreeDeliveryThreshold(new BigDecimal("10.00"));
        storeDeliverySetting.setMaxDistance(new BigDecimal("20.00"));
        storeDeliverySetting.setDeliveryGeography(DeliveryGeography.ONLY_IN_THE_CITY);
        storeDeliverySetting.setStore(store);
    }

    private StoreDeliverySettingRequest defaultRequest() {
        return new StoreDeliverySettingRequest(
                true,
                DeliveryType.SAME_DAY,
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                DeliveryGeography.ONLY_IN_THE_CITY
        );
    }

    @Test
    void createStoreDeliverySetting_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliverySettingRepository.existsByStoreId(1L)).thenReturn(false);
        when(storeDeliverySettingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoreDeliverySettingResponse response = storeDeliverySettingService.createStoreDeliverySetting(1L, defaultRequest());
        verify(storeDeliverySettingRepository).save(any());

        assertEquals(true, response.deliveryEnabled());
        assertEquals(DeliveryType.SAME_DAY, response.deliveryType());
        assertEquals(new BigDecimal("10.00"), response.minOrderPrice());
        assertEquals(new BigDecimal("10.00"), response.freeDeliveryThreshold());
        assertEquals(new BigDecimal("20.00"), response.maxDistance());
        assertEquals(DeliveryGeography.ONLY_IN_THE_CITY, response.deliveryGeography());
    }

    @Test
    void createStoreDeliverySetting_shouldThrowException_whenSettingAlreadyExists() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliverySettingRepository.existsByStoreId(1L)).thenReturn(true);

        assertThrows(StoreDeliverySettingAlreadyExistsException.class,
                () -> storeDeliverySettingService.createStoreDeliverySetting(1L, defaultRequest()));

        verify(storeDeliverySettingRepository, never()).save(any());
    }

    @Test
    void getStoreDeliverySetting_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));

        StoreDeliverySettingResponse response = storeDeliverySettingService.getStoreDeliverySetting(1L);

        assertEquals(1L, response.id());
        assertEquals(true, response.deliveryEnabled());
        assertEquals(DeliveryType.SAME_DAY, response.deliveryType());
        assertEquals(new BigDecimal("10.00"), response.minOrderPrice());
        assertEquals(new BigDecimal("10.00"), response.freeDeliveryThreshold());
        assertEquals(new BigDecimal("20.00"), response.maxDistance());
        assertEquals(DeliveryGeography.ONLY_IN_THE_CITY, response.deliveryGeography());
    }

    @Test
    void getStoreDeliverySetting_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> storeDeliverySettingService.getStoreDeliverySetting(1L));
    }

    @Test
    void updateStoreDeliverySetting_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliverySettingRepository.save(any())).thenReturn(storeDeliverySetting);

        StoreDeliverySettingRequest updatedRequest = new StoreDeliverySettingRequest(
                false,
                DeliveryType.SCHEDULED,
                new BigDecimal("20.00"),
                new BigDecimal("20.00"),
                new BigDecimal("30.00"),
                DeliveryGeography.CITY_AND_SUBURBS
        );

        StoreDeliverySettingResponse response = storeDeliverySettingService.updateStoreDeliverySetting(1L, updatedRequest);

        verify(storeDeliverySettingRepository).save(any());

        assertEquals(false, response.deliveryEnabled());
        assertEquals(DeliveryType.SCHEDULED, response.deliveryType());
        assertEquals(new BigDecimal("20.00"), response.minOrderPrice());
        assertEquals(new BigDecimal("20.00"), response.freeDeliveryThreshold());
        assertEquals(new BigDecimal("30.00"), response.maxDistance());
        assertEquals(DeliveryGeography.CITY_AND_SUBURBS, response.deliveryGeography());
    }

    @Test
    void updateStoreDeliverySetting_shouldThrowException_whenSettingNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.empty());

        assertThrows(StoreDeliverySettingNotFoundException.class,
                () -> storeDeliverySettingService.updateStoreDeliverySetting(1L, defaultRequest()));

        verify(storeDeliverySettingRepository, never()).save(any());
    }

}
