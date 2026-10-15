package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupRequest;
import com.delivery.habsida.dto.ModifierGroupResponse;
import com.delivery.habsida.service.ModifierGroupService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/modifier-groups")
public class ModifierGroupController {
    private final ModifierGroupService modifierGroupService;

    public ModifierGroupController(ModifierGroupService modifierGroupService) {
        this.modifierGroupService = modifierGroupService;
    }

    @PostMapping
    public ResponseEntity<ModifierGroupResponse> createModifierGroup(@PathVariable Long storeId,
                                                                     @RequestBody @Valid ModifierGroupRequest request) {
        ModifierGroupResponse created = modifierGroupService.createModifierGroup(storeId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public Page<ModifierGroupResponse> getModifierGroups(@PathVariable Long storeId, @ParameterObject Pageable pageable) {
        return modifierGroupService.getModifierGroups(storeId, pageable);
    }

    @GetMapping("/{groupId}")
    public ModifierGroupResponse getModifierGroup(@PathVariable Long storeId,
                                                  @PathVariable Long groupId) {
        return modifierGroupService.getModifierGroup(storeId, groupId);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ModifierGroupResponse> updateModifierGroup(@PathVariable Long storeId,
                                                                     @PathVariable Long groupId,
                                                                     @RequestBody @Valid ModifierGroupRequest request) {
        ModifierGroupResponse updated = modifierGroupService.updateModifierGroup(storeId, groupId, request);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteModifierGroup(@PathVariable Long storeId,
                                                    @PathVariable Long groupId) {
        modifierGroupService.deleteModifierGroup(storeId, groupId);

        return ResponseEntity.noContent().build();
    }
}
