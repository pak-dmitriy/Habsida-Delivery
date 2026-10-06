package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreDeliverySettingRequest;
import com.delivery.habsida.dto.StoreDeliverySettingResponse;
import com.delivery.habsida.service.StoreDeliverySettingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/delivery-settings")
public class StoreDeliverySettingController {
    private final StoreDeliverySettingService storeDeliverySettingService;

    public StoreDeliverySettingController(StoreDeliverySettingService storeDeliverySettingService) {
        this.storeDeliverySettingService = storeDeliverySettingService;
    }

    @PostMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliverySettingResponse> createStoreDeliverySetting(@PathVariable Long storeId,
                                                                                   @RequestBody @Valid StoreDeliverySettingRequest request) {
        StoreDeliverySettingResponse response = storeDeliverySettingService.createStoreDeliverySetting(storeId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliverySettingResponse> getStoreDeliverySetting(@PathVariable Long storeId) {
        StoreDeliverySettingResponse response = storeDeliverySettingService.getStoreDeliverySetting(storeId);

        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliverySettingResponse> updateStoreDeliverySetting(@PathVariable Long storeId,
                                                                                   @RequestBody @Valid StoreDeliverySettingRequest request) {
        StoreDeliverySettingResponse response = storeDeliverySettingService.updateStoreDeliverySetting(storeId, request);

        return ResponseEntity.ok(response);
    }
}
