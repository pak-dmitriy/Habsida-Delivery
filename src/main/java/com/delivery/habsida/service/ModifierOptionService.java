package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierOptionRequest;
import com.delivery.habsida.dto.ModifierOptionResponse;
import com.delivery.habsida.entity.ModifierGroup;
import com.delivery.habsida.entity.ModifierOption;
import com.delivery.habsida.exception.InvalidModifierOptionException;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.exception.ModifierOptionNotFoundException;
import com.delivery.habsida.repository.ModifierGroupRepository;
import com.delivery.habsida.repository.ModifierOptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ModifierOptionService {
    private final ModifierOptionRepository modifierOptionRepository;
    private final ModifierGroupRepository modifierGroupRepository;

    public ModifierOptionService(ModifierOptionRepository modifierOptionRepository,
                                 ModifierGroupRepository modifierGroupRepository) {
        this.modifierOptionRepository = modifierOptionRepository;
        this.modifierGroupRepository = modifierGroupRepository;
    }

    private ModifierGroup getModifierGroupOrThrow(Long storeId, Long groupId) {
        return modifierGroupRepository.findByIdAndStoreId(groupId, storeId)
                .orElseThrow(() -> new ModifierGroupNotFoundException("Modifier group not found"));
    }

    private ModifierOption getModifierOptionOrThrow(Long storeId, Long groupId, Long optionId) {
        getModifierGroupOrThrow(storeId, groupId);
        return modifierOptionRepository.findByIdAndModifierGroupId(optionId, groupId)
                .orElseThrow(() -> new ModifierOptionNotFoundException("Option not found"));
    }

    private void validatePriceAndIsFree(BigDecimal priceDelta, Boolean isFree) {
        if (isFree && priceDelta.compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidModifierOptionException("Price must be 0 for a free option");
        }
        if (!isFree && priceDelta.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidModifierOptionException("Price must be greater than 0 for a paid option");
        }
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ModifierOptionResponse createModifierOption(Long storeId,
                                                       Long groupId,
                                                       ModifierOptionRequest request) {
        validatePriceAndIsFree(request.priceDelta(), request.isFree());

        ModifierOption modifierOption = new ModifierOption();

        ModifierGroup modifierGroup = getModifierGroupOrThrow(storeId, groupId);
        modifierOption.setName(request.name());
        modifierOption.setPriceDelta(request.priceDelta());
        modifierOption.setIsFree(request.isFree());
        modifierOption.setModifierGroup(modifierGroup);

        return ModifierOptionResponse.from(modifierOptionRepository.save(modifierOption));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ModifierOptionResponse getModifierOption(Long storeId, Long groupId, Long optionId) {
        ModifierOption modifierOption = getModifierOptionOrThrow(storeId, groupId, optionId);

        return ModifierOptionResponse.from(modifierOption);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public Page<ModifierOptionResponse> getModifierOptions(Long storeId, Long groupId, Pageable pageable) {
        getModifierGroupOrThrow(storeId, groupId);

        return modifierOptionRepository.findByModifierGroupIdOrderByIdAsc(groupId, pageable)
                .map(ModifierOptionResponse::from);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ModifierOptionResponse updateModifierOption(Long storeId,
                                                       Long groupId,
                                                       Long optionId,
                                                       ModifierOptionRequest request) {
        validatePriceAndIsFree(request.priceDelta(), request.isFree());

        ModifierOption modifierOption = getModifierOptionOrThrow(storeId, groupId, optionId);
        modifierOption.setName(request.name());
        modifierOption.setPriceDelta(request.priceDelta());
        modifierOption.setIsFree(request.isFree());

        return ModifierOptionResponse.from(modifierOptionRepository.save(modifierOption));
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public void deleteModifierOption(Long storeId, Long groupId, Long optionId) {
       ModifierOption modifierOption = getModifierOptionOrThrow(storeId, groupId, optionId);

       modifierOptionRepository.delete(modifierOption);
    }
}

