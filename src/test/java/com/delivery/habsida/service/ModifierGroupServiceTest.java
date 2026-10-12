package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierGroupResponse;
import com.delivery.habsida.dto.ModifierGroupRequest;
import com.delivery.habsida.entity.ModifierGroup;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.InvalidQuantityException;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.repository.ModifierGroupRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModifierGroupServiceTest {
    @Mock
    private ModifierGroupRepository modifierGroupRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ModifierGroupService modifierGroupService;

    private ModifierGroup modifierGroup;
    private Store store;

    @BeforeEach
    void setUp() {
        modifierGroup = new ModifierGroup();
        store = new Store();

        store.setId(1L);

        modifierGroup.setId(1L);
        modifierGroup.setName("group");
        modifierGroup.setRequired(true);
        modifierGroup.setMinSelect(1);
        modifierGroup.setMaxSelect(5);
        modifierGroup.setStore(store);
    }

    @Test
    void createModifierGroup_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(modifierGroupRepository.save(any())).thenReturn(modifierGroup);

        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );

        ModifierGroupResponse response = modifierGroupService.createModifierGroup(1L, request);
        assertEquals("group", response.name());
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());
    }

    @Test
    void createModifierGroup_invalidQuantity_throwsException() {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                5,
                1
        );
        assertThrows(InvalidQuantityException.class,
                () -> modifierGroupService.createModifierGroup(1L, request));
    }

    @Test
    void getModifierGroup_success() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));

        ModifierGroupResponse response = modifierGroupService.getModifierGroup(1L, 1L);
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());
    }

    @Test
    void getModifierGroup_notFound_throwsException() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierGroupNotFoundException.class,
                () -> modifierGroupService.getModifierGroup(1L, 1L));
    }

    @Test
    void getModifierGroups_success() {
        when(modifierGroupRepository.findByStoreId(1L)).thenReturn(List.of(modifierGroup));

        List<ModifierGroupResponse> response = modifierGroupService.getModifierGroups(1L);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
    }

    @Test
    void getModifierGroups_returnsEmptyList_whenNoGroups() {
        when(modifierGroupRepository.findByStoreId(1L)).thenReturn(Collections.emptyList());

        List<ModifierGroupResponse> response = modifierGroupService.getModifierGroups(1L);
        assertTrue(response.isEmpty());
    }

    @Test
    void updateModifierGroup_success() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierGroupRepository.save(any())).thenReturn(modifierGroup);

        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );
        ModifierGroupResponse response = modifierGroupService.updateModifierGroup(1L, 1L, request);
        assertEquals("group", response.name());
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());
    }

    @Test
    void updateModifierGroup_notFound_throwsException() {
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );
        assertThrows(ModifierGroupNotFoundException.class,
                ()->modifierGroupService.updateModifierGroup(1L, 1L, request));
    }

    @Test
    void updateModifierGroup_InvalidQuantity_throwsException() {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                5,
                1
        );
        assertThrows(InvalidQuantityException.class,
                ()-> modifierGroupService.updateModifierGroup(1L, 1L, request));
    }

    @Test
    void deleteModifierGroup_success() {
       when( modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));
       modifierGroupService.deleteModifierGroup(1L, 1L);
       verify(modifierGroupRepository).delete(modifierGroup);
    }

    @Test
    void deleteModifierGroup_notFound_throwsException() {
        when( modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(ModifierGroupNotFoundException.class,
                ()-> modifierGroupService.deleteModifierGroup(1L, 1L));
    }
}
