package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierOptionRequest;
import com.delivery.habsida.dto.ModifierOptionResponse;
import com.delivery.habsida.service.ModifierOptionService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/modifier-groups/{groupId}/options")
public class ModifierOptionController {
    private final ModifierOptionService modifierOptionService;

    public ModifierOptionController(ModifierOptionService modifierOptionService) {
        this.modifierOptionService = modifierOptionService;
    }

    @PostMapping
    public ResponseEntity<ModifierOptionResponse> createModifierOption(@PathVariable Long storeId,
                                                                       @PathVariable Long groupId,
                                                                       @RequestBody @Valid ModifierOptionRequest request) {
        ModifierOptionResponse created = modifierOptionService.createModifierOption(storeId, groupId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{optionId}")
    public ModifierOptionResponse getModifierOption(@PathVariable Long storeId,
                                                    @PathVariable Long groupId,
                                                    @PathVariable Long optionId) {
        return modifierOptionService.getModifierOption(storeId, groupId, optionId);
    }

    @GetMapping
    public Page<ModifierOptionResponse> getModifierOptions(@PathVariable Long storeId,
                                                           @PathVariable Long groupId, @ParameterObject Pageable pageable) {
        return modifierOptionService.getModifierOptions(storeId, groupId, pageable);
    }

    @PutMapping("/{optionId}")
    public ResponseEntity<ModifierOptionResponse> updateModifierOption(@PathVariable Long storeId,
                                                                       @PathVariable Long groupId,
                                                                       @PathVariable Long optionId,
                                                                       @RequestBody @Valid ModifierOptionRequest request) {
        ModifierOptionResponse updated = modifierOptionService.updateModifierOption(storeId, groupId, optionId, request);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deleteModifierOption(@PathVariable Long storeId,
                                                     @PathVariable Long groupId,
                                                     @PathVariable Long optionId) {

        modifierOptionService.deleteModifierOption(storeId, groupId, optionId);
        return ResponseEntity.noContent().build();
    }
}
