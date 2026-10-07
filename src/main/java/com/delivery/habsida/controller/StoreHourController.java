package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreHourDto;
import com.delivery.habsida.dto.StoreHourRequest;
import com.delivery.habsida.service.StoreHourService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores/{storeId}/hours")
public class StoreHourController {
    private final StoreHourService storeHourService;
    public StoreHourController(StoreHourService storeHourService) {
        this.storeHourService = storeHourService;
    }

    @PostMapping
    public ResponseEntity<StoreHourDto> createStoreHour(@PathVariable Long storeId,
                                                        @RequestBody @Valid StoreHourRequest request) {
        StoreHourDto created = storeHourService.createStoreHour(storeId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<StoreHourDto> getStoreHours(@PathVariable Long storeId) {
        return storeHourService.getStoreHours(storeId);
    }

    @GetMapping("/{hourId}")
    public StoreHourDto getStoreHour(@PathVariable Long storeId,
                                     @PathVariable Long hourId) {
        return storeHourService.getStoreHour(storeId, hourId);
    }

    @PutMapping("/{hourId}")
    public ResponseEntity<StoreHourDto> updateStoreHour(@PathVariable Long storeId,
                                                        @PathVariable Long hourId,
                                                        @RequestBody @Valid StoreHourRequest request) {
        StoreHourDto updated = storeHourService.updateStoreHour(storeId, hourId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{hourId}")
    public ResponseEntity<Void> deleteStoreHour(@PathVariable Long storeId,
                                                @PathVariable Long hourId) {
        storeHourService.deleteStoreHour(storeId, hourId);
        return ResponseEntity.noContent().build();
    }
}
