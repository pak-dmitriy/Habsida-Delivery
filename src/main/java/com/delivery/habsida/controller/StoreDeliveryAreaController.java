package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreDeliveryAreaRequest;
import com.delivery.habsida.dto.StoreDeliveryAreaResponse;
import com.delivery.habsida.service.StoreDeliveryAreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores/{storeId}/delivery-areas")
public class StoreDeliveryAreaController {
    private final StoreDeliveryAreaService storeDeliveryAreaService;

    public StoreDeliveryAreaController(StoreDeliveryAreaService storeDeliveryAreaService) {
        this.storeDeliveryAreaService = storeDeliveryAreaService;
    }

    @PostMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliveryAreaResponse> createStoreDeliveryArea(@PathVariable Long storeId,
                                                                             @RequestBody @Valid StoreDeliveryAreaRequest request) {
        StoreDeliveryAreaResponse response = storeDeliveryAreaService.createStoreDeliveryArea(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public List<StoreDeliveryAreaResponse> getStoreDeliveryAreas(@PathVariable Long storeId) {
        return storeDeliveryAreaService.getStoreDeliveryAreas(storeId);
    }

    @GetMapping("/{areaId}")
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public StoreDeliveryAreaResponse getStoreDeliveryArea(@PathVariable Long storeId,
                                                          @PathVariable Long areaId) {
        return storeDeliveryAreaService.getStoreDeliveryArea(storeId, areaId);
    }

    @PutMapping("/{areaId}")
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<StoreDeliveryAreaResponse> updateStoreDeliveryArea(@PathVariable Long storeId,
                                                                             @PathVariable Long areaId,
                                                                             @RequestBody @Valid StoreDeliveryAreaRequest request) {
        StoreDeliveryAreaResponse updatedArea = storeDeliveryAreaService.updateStoreDeliveryArea(storeId, areaId, request);
        return ResponseEntity.ok().body(updatedArea);
    }

    @DeleteMapping("/{areaId}")
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<Void> deleteStoreDeliveryArea(@PathVariable Long storeId,
                                                        @PathVariable Long areaId) {
        storeDeliveryAreaService.deleteStoreDeliveryArea(storeId, areaId);
        return ResponseEntity.noContent().build();
    }
}
