package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierOptionResponse;
import com.delivery.habsida.dto.ModifierOptionRequest;
import com.delivery.habsida.entity.*;
import com.delivery.habsida.exception.InvalidModifierOptionException;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.exception.ModifierOptionNotFoundException;
import com.delivery.habsida.repository.ModifierGroupRepository;
import com.delivery.habsida.repository.ModifierOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModifierOptionServiceTest {
    @Mock
    private ModifierOptionRepository modifierOptionRepository;

    @Mock
    private ModifierGroupRepository modifierGroupRepository;

    @InjectMocks
    private ModifierOptionService modifierOptionService;

    private ModifierOption modifierOption;
    private ModifierGroup modifierGroup;

    @BeforeEach
    void setUp() {
        modifierOption = new ModifierOption();
        modifierGroup = new ModifierGroup();

        modifierOption.setName("option");
        modifierOption.setPriceDelta(new BigDecimal(0));
        modifierOption.setIsFree(true);
        modifierOption.setModifierGroup(modifierGroup);

        modifierGroup.setName("group");
        modifierGroup.setRequired(true);
        modifierGroup.setMinSelect(1);
        modifierGroup.setMaxSelect(5);
        modifierGroup.setId(1L);
        modifierOption.setId(1L);

        Store store = new Store();
        store.setId(1L);
        modifierGroup.setStore(store);
    }

    @Test
    void createModifierOption_success() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.save(any())).thenReturn(modifierOption);

        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(0),
                true
        );

        ModifierOptionResponse result = modifierOptionService.createModifierOption(1L, 1L, request);
        assertEquals("option", result.name());
        assertEquals(new BigDecimal(0), result.priceDelta());
        assertTrue(result.isFree());
    }

    @Test
    void createModifierOption_priceIsFreeMismatch_throwsException() {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(0),
                false
        );
        assertThrows(InvalidModifierOptionException.class,
                () -> modifierOptionService.createModifierOption(1L, 1L, request));
    }

    @Test
    void getModifierOption_success() {
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.of(modifierOption));
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));

        ModifierOptionResponse modifierOptionResponse = modifierOptionService.getModifierOption(1L, 1L, 1L);
        assertEquals(1L, modifierOptionResponse.id());
        assertEquals("option", modifierOptionResponse.name());
    }

    @Test
    void getModifierOption_notFound_throwsException() {
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.empty());
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));

        assertThrows(ModifierOptionNotFoundException.class,
                () -> modifierOptionService.getModifierOption(1L, 1L, 1L));
    }

    @Test
    void getModifierOptions_success() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.findByModifierGroupIdOrderByIdAsc(1L)).thenReturn(List.of(modifierOption));

        List<ModifierOptionResponse> list = modifierOptionService.getModifierOptions(1L, 1L);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).id());
    }

    @Test
    void getModifierOptions_groupNotFound_throwsException() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierGroupNotFoundException.class,
                () -> modifierOptionService.getModifierOptions(1L, 1L));
    }

    @Test
    void updateModifierOption_success() {
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.of(modifierOption));
        when(modifierOptionRepository.save(any())).thenReturn(modifierOption);
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));

        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(0),
                true
        );
        ModifierOptionResponse result = modifierOptionService.updateModifierOption(1L, 1L, 1L, request);
        assertEquals("option", result.name());
        assertEquals(new BigDecimal(0), result.priceDelta());
        assertTrue(result.isFree());
    }

    @Test
    void updateModifierOption_notFound_throwsException() {
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.empty());
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));

        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(0),
                true
        );
        assertThrows(ModifierOptionNotFoundException.class,
                ()-> modifierOptionService.updateModifierOption(1L, 1L, 1L, request));
    }

    @Test
    void updateModifierOption_priceIsFreeMismatch_throwsException() {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(0),
                false
        );
        assertThrows(InvalidModifierOptionException.class,
                () -> modifierOptionService.updateModifierOption(1L, 1L, 1L, request));
    }

    @Test
    void deleteModifierOption_success() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.of(modifierOption));

        modifierOptionService.deleteModifierOption(1L, 1L, 1L);
        verify(modifierOptionRepository).delete(modifierOption);
    }

    @Test
    void deleteModifierOption_notFound_throwsException() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierOptionNotFoundException.class,
                ()-> modifierOptionService.deleteModifierOption(1L, 1L, 1L));
    }

}
