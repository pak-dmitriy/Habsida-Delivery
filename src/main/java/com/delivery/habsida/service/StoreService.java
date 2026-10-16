package com.delivery.habsida.service;

import com.delivery.habsida.dto.StoreCreateRequest;
import com.delivery.habsida.dto.StoreResponse;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.StoreRepository;
import com.delivery.habsida.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StoreResponse createStore(StoreCreateRequest storeCreateRequest) {
        Store store = new Store();

        store.setName(storeCreateRequest.name());
        store.setStoreSlug(storeCreateRequest.storeSlug());
        store.setDescription(storeCreateRequest.description());
        store.setTypeStoreServices(storeCreateRequest.typeStoreServices());
        store.setPhone(storeCreateRequest.phone());
        store.setLogo(storeCreateRequest.logo());
        store.setStatus(storeCreateRequest.status());
        store.setPickupAddress(storeCreateRequest.pickupAddress());
        store.setDeliveryFee(storeCreateRequest.deliveryFee());

        Store savedStore = storeRepository.save(store);

        return StoreResponse.from(savedStore);
    }

    public Page<StoreResponse> getStores(Authentication authentication, Pageable pageable) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return storeRepository.findAll(pageable)
                    .map(StoreResponse::from);
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<Store> stores = storeRepository.findStoresByUserId(userPrincipal.userId(), pageable);

        return stores
                .map(StoreResponse::from);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreResponse getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        return StoreResponse.from(store);
    }
}
