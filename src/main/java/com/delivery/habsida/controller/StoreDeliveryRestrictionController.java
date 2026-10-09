package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreDeliveryRestrictionRequest;
import com.delivery.habsida.dto.StoreDeliveryRestrictionResponse;
import com.delivery.habsida.service.StoreDeliveryRestrictionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores/{storeId}/delivery-restrictions")
public class StoreDeliveryRestrictionController {
    private final StoreDeliveryRestrictionService storeDeliveryRestrictionService;

    public StoreDeliveryRestrictionController(StoreDeliveryRestrictionService storeDeliveryRestrictionService) {
        this.storeDeliveryRestrictionService = storeDeliveryRestrictionService;
    }

    @PostMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliveryRestrictionResponse> createStoreDeliveryRestriction(@PathVariable Long storeId,
                                                                                           @RequestBody @Valid StoreDeliveryRestrictionRequest request) {
        StoreDeliveryRestrictionResponse response = storeDeliveryRestrictionService.createStoreDeliveryRestriction(storeId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public List<StoreDeliveryRestrictionResponse> getStoreDeliveryRestrictions(@PathVariable Long storeId) {

        return storeDeliveryRestrictionService.getStoreDeliveryRestrictions(storeId);
    }

    @GetMapping("/{restrictionId}")
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreDeliveryRestrictionResponse getStoreDeliveryRestriction(@PathVariable Long storeId,
                                                                                        @PathVariable Long restrictionId) {

        return storeDeliveryRestrictionService.getStoreDeliveryRestriction(storeId, restrictionId);
    }

    @PutMapping("/{restrictionId}")
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliveryRestrictionResponse> updateStoreDeliveryRestriction(@PathVariable Long storeId,
                                                                                           @PathVariable Long restrictionId,
                                                                                           @RequestBody @Valid StoreDeliveryRestrictionRequest request) {
        StoreDeliveryRestrictionResponse response = storeDeliveryRestrictionService.updateStoreDeliveryRestriction(storeId, restrictionId, request);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{restrictionId}")
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<Void> deleteStoreDeliveryRestriction(@PathVariable Long storeId,
                                                               @PathVariable Long restrictionId) {
        storeDeliveryRestrictionService.deleteStoreDeliveryRestriction(storeId, restrictionId);

        return ResponseEntity.noContent().build();
    }
}
