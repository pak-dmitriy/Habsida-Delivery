package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreCreateRequest;
import com.delivery.habsida.dto.StoreResponse;
import com.delivery.habsida.entity.Status;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.TypeStoreServices;
import com.delivery.habsida.entity.UserStoreAccess;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreRepository;
import com.delivery.habsida.repository.UserStoreAccessRepository;
import com.delivery.habsida.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class StoreServiceTest {
    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserStoreAccessRepository userStoreAccessRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StoreService storeService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private Store defaultStore() {
        Store store = new Store();

        store.setId(2L);
        store.setName("Sushi House");
        store.setStoreSlug("sushi-house");
        store.setDescription("Best sushi in town");
        store.setTypeStoreServices(TypeStoreServices.RESTAURANT);
        store.setPhone("+998901234567");
        store.setLogo("logo.png");
        store.setPickupAddress("Tashkent, Amir Temur 1");
        store.setStatus(Status.ACTIVE);
        store.setDeliveryFee(new BigDecimal("5.00"));

        return store;
    }

    private StoreCreateRequest defaultRequest() {
        return new StoreCreateRequest(
                "Sushi House",
                "sushi-house",
                "Best sushi in town",
                TypeStoreServices.RESTAURANT,
                "+998901234567",
                "logo.png",
                "Tashkent, Amir Temur 1",
                Status.ACTIVE,
                new BigDecimal("5.00")
        );
    }

    @Test
    void createStore_shouldCreateStoreAndReturnResponse() {
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreResponse response = storeService.createStore(defaultRequest());

        assertEquals("Sushi House", response.name());
        assertEquals("sushi-house", response.storeSlug());
        assertEquals("Best sushi in town", response.description());
        assertEquals(TypeStoreServices.RESTAURANT, response.typeStoreServices());
        assertEquals("+998901234567", response.phone());
        assertEquals("logo.png", response.logo());
        assertEquals("Tashkent, Amir Temur 1", response.pickupAddress());
        assertEquals(Status.ACTIVE, response.status());
        assertEquals(new BigDecimal("5.00"), response.deliveryFee());

        verify(storeRepository).save(any());
    }

    @Test
    void getStores_shouldReturnAllStores_whenUserIsAdmin() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();
        when(storeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(defaultStore())));

        Page<StoreResponse> page = storeService.getStores(authentication, pageable);
        StoreResponse response = page.getContent().get(0);

        assertEquals(2L, response.id());
        assertEquals("Sushi House", response.name());
        assertEquals("sushi-house", response.storeSlug());
        assertEquals("Best sushi in town", response.description());
        assertEquals(TypeStoreServices.RESTAURANT, response.typeStoreServices());
        assertEquals("+998901234567", response.phone());
        assertEquals("logo.png", response.logo());
        assertEquals("Tashkent, Amir Temur 1", response.pickupAddress());
        assertEquals(Status.ACTIVE, response.status());
        assertEquals(new BigDecimal("5.00"), response.deliveryFee());

        verify(storeRepository).findAll(pageable);
        verify(userStoreAccessRepository, never()).findAllByUserId(anyLong());
    }

    @Test
    void getStores_shouldReturnStores_whenUserIsMerchant() {
        UserStoreAccess userStoreAccess = new UserStoreAccess();
        userStoreAccess.setStore(defaultStore());

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(3L, "merchant@merchant"));
        when(storeRepository.findStoresByUserId(3L, pageable)).thenReturn(new PageImpl<>(List.of(defaultStore())));

        Page<StoreResponse> page = storeService.getStores(authentication, pageable);
        StoreResponse response = page.getContent().get(0);

        assertEquals(2L, response.id());
        assertEquals("Sushi House", response.name());
        assertEquals("sushi-house", response.storeSlug());
        assertEquals("Best sushi in town", response.description());
        assertEquals(TypeStoreServices.RESTAURANT, response.typeStoreServices());
        assertEquals("+998901234567", response.phone());
        assertEquals("logo.png", response.logo());
        assertEquals("Tashkent, Amir Temur 1", response.pickupAddress());
        assertEquals(Status.ACTIVE, response.status());
        assertEquals(new BigDecimal("5.00"), response.deliveryFee());

        verify(storeRepository).findStoresByUserId(3L, pageable);
    }

    @Test
    void getStores_shouldReturnEmptyList_whenMerchantHasNoStores() {

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(new UserPrincipal(3L, "merchant@merchant"));
        when(storeRepository.findStoresByUserId(3L, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<StoreResponse> result = storeService.getStores(authentication, pageable);

        assertTrue(result.isEmpty());

        verify(storeRepository).findStoresByUserId(3L, pageable);
        verify(storeRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getStore_shouldReturnStore_whenStoreExists() {
        when(storeRepository.findById(2L)).thenReturn(Optional.of(defaultStore()));

        StoreResponse response = storeService.getStore(2L);

        assertEquals(2L, response.id());
        assertEquals("Sushi House", response.name());
        assertEquals("sushi-house", response.storeSlug());
        assertEquals("Best sushi in town", response.description());
        assertEquals(TypeStoreServices.RESTAURANT, response.typeStoreServices());
        assertEquals("+998901234567", response.phone());
        assertEquals("logo.png", response.logo());
        assertEquals("Tashkent, Amir Temur 1", response.pickupAddress());
        assertEquals(Status.ACTIVE, response.status());
        assertEquals(new BigDecimal("5.00"), response.deliveryFee());

        verify(storeRepository).findById(2L);
    }

    @Test
    void getStore_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> storeService.getStore(99L));

        verify(storeRepository).findById(99L);
    }
}
