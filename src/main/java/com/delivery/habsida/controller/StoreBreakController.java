package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreBreakResponse;
import com.delivery.habsida.dto.StoreBreakRequest;
import com.delivery.habsida.service.StoreBreakService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/hours/{hourId}/breaks")
public class StoreBreakController {
    private final StoreBreakService storeBreakService;

    public StoreBreakController(StoreBreakService storeBreakService) {
        this.storeBreakService = storeBreakService;
    }

    @PostMapping
    public ResponseEntity<StoreBreakResponse> createStoreBreak(@PathVariable Long storeId,
                                                               @PathVariable Long hourId,
                                                               @Valid @RequestBody StoreBreakRequest storeBreakRequest) {
        StoreBreakResponse created = storeBreakService.createStoreBreak(storeId, hourId, storeBreakRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Page<StoreBreakResponse> getStoreBreaks(@PathVariable Long storeId,
                                                   @PathVariable Long hourId, Pageable pageable) {
        return storeBreakService.getStoreBreaks(storeId, hourId, pageable);
    }

    @GetMapping("/{breakId}")
    public StoreBreakResponse getStoreBreak(@PathVariable Long storeId,
                                            @PathVariable Long hourId,
                                            @PathVariable Long breakId) {
        return storeBreakService.getStoreBreak(storeId, hourId, breakId);
    }

    @PutMapping("/{breakId}")
    public ResponseEntity<StoreBreakResponse> updateStoreBreak(@PathVariable Long storeId,
                                                               @PathVariable Long hourId,
                                                               @PathVariable Long breakId,
                                                               @RequestBody @Valid StoreBreakRequest request) {
        StoreBreakResponse updated = storeBreakService.updateStoreBreak(storeId, hourId, breakId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{breakId}")
    public ResponseEntity<Void> deleteStoreBreak(@PathVariable Long storeId,
                                                 @PathVariable Long hourId,
                                                 @PathVariable Long breakId) {
        storeBreakService.deleteStoreBreak(storeId, hourId, breakId);
        return ResponseEntity.noContent().build();
    }

}
