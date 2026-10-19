package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierGroupRequest;
import com.delivery.habsida.dto.ModifierGroupResponse;
import com.delivery.habsida.entity.ModifierGroup;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.InvalidQuantityException;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.ModifierGroupRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ModifierGroupService {

    private final ModifierGroupRepository modifierGroupRepository;
    private final StoreRepository storeRepository;

    public ModifierGroupService(ModifierGroupRepository modifierGroupRepository, StoreRepository storeRepository) {
        this.modifierGroupRepository = modifierGroupRepository;
        this.storeRepository = storeRepository;
    }

    private ModifierGroup getModifierGroupOrThrow(Long storeId, Long id) {
        return modifierGroupRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ModifierGroupNotFoundException("Modifier group not found"));
    }

    private void validateSelectRange(ModifierGroupRequest request) {
        if (request.minSelect() > request.maxSelect()) {
            throw new InvalidQuantityException("minSelect cannot be greater than maxSelect");
        }
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ModifierGroupResponse createModifierGroup(Long storeId, ModifierGroupRequest request) {
        validateSelectRange(request);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        ModifierGroup modifierGroup = new ModifierGroup();
        modifierGroup.setStore(store);
        modifierGroup.setName(request.name());
        modifierGroup.setRequired(request.required());
        modifierGroup.setMinSelect(request.minSelect());
        modifierGroup.setMaxSelect(request.maxSelect());

        return ModifierGroupResponse.from(modifierGroupRepository.save(modifierGroup));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ModifierGroupResponse getModifierGroup(Long storeId, Long groupId) {
        ModifierGroup modifierGroup = getModifierGroupOrThrow(storeId, groupId);

        return ModifierGroupResponse.from(modifierGroup);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public Page<ModifierGroupResponse> getModifierGroups(Long storeId, Pageable pageable) {
        return modifierGroupRepository.findByStoreId(storeId, pageable)
                .map(ModifierGroupResponse::from);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ModifierGroupResponse updateModifierGroup(Long storeId, Long groupId, ModifierGroupRequest request) {
        validateSelectRange(request);

        ModifierGroup modifierGroup = getModifierGroupOrThrow(storeId, groupId);
        modifierGroup.setName(request.name());
        modifierGroup.setRequired(request.required());
        modifierGroup.setMinSelect(request.minSelect());
        modifierGroup.setMaxSelect(request.maxSelect());

        return ModifierGroupResponse.from(modifierGroupRepository.save(modifierGroup));
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteModifierGroup(Long storeId, Long groupId) {
        ModifierGroup modifierGroup = getModifierGroupOrThrow(storeId, groupId);

        modifierGroupRepository.delete(modifierGroup);
    }
}

