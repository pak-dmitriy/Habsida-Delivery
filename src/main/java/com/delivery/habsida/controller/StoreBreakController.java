package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreBreakDto;
import com.delivery.habsida.dto.StoreBreakRequest;
import com.delivery.habsida.service.StoreBreakService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores/{storeId}/hours/{hourId}/breaks")
public class StoreBreakController {
    private final StoreBreakService storeBreakService;

    public StoreBreakController(StoreBreakService storeBreakService) {
        this.storeBreakService = storeBreakService;
    }

    @PostMapping
    public ResponseEntity<StoreBreakDto> createStoreBreak(@PathVariable Long storeId,
                                                          @PathVariable Long hourId,
                                                          @Valid @RequestBody StoreBreakRequest storeBreakRequest) {
        StoreBreakDto created = storeBreakService.createStoreBreak(storeId, hourId, storeBreakRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<StoreBreakDto> getStoreBreaks(@PathVariable Long storeId,
                                              @PathVariable Long hourId) {
        return storeBreakService.getStoreBreaks(storeId, hourId);
    }

    @GetMapping("/{breakId}")
    public StoreBreakDto getStoreBreak(@PathVariable Long storeId,
                                       @PathVariable Long hourId,
                                       @PathVariable Long breakId) {
        return storeBreakService.getStoreBreak(storeId, hourId, breakId);
    }

    @PutMapping("/{breakId}")
    public ResponseEntity<StoreBreakDto> updateStoreBreak(@PathVariable Long storeId,
                                                          @PathVariable Long hourId,
                                                          @PathVariable Long breakId,
                                                          @RequestBody @Valid StoreBreakRequest request) {
        StoreBreakDto updated = storeBreakService.updateStoreBreak(storeId, hourId, breakId, request);
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
