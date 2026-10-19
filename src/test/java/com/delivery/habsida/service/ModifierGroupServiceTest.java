package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierGroupRequest;
import com.delivery.habsida.dto.ModifierGroupResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModifierGroupServiceTest {
    @Mock
    private ModifierGroupRepository modifierGroupRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ModifierGroupService modifierGroupService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private ModifierGroup modifierGroup;
    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        modifierGroup = new ModifierGroup();
        modifierGroup.setId(5L);
        modifierGroup.setStore(store);
        modifierGroup.setName("group");
        modifierGroup.setRequired(true);
        modifierGroup.setMinSelect(1);
        modifierGroup.setMaxSelect(5);
    }

    private ModifierGroupRequest defaultRequest() {
        return new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );
    }

    @Test
    void createModifierGroup_success() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(modifierGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ModifierGroupResponse response = modifierGroupService.createModifierGroup(1L, defaultRequest());

        assertEquals(1L, response.storeId());
        assertEquals("group", response.name());
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());

        verify(modifierGroupRepository).save(any());
    }

    @Test
    void createModifierGroup_throwsException_whenQuantityIsInvalid() {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                5,
                1
        );
        assertThrows(InvalidQuantityException.class,
                () -> modifierGroupService.createModifierGroup(1L, request));

        verify(modifierGroupRepository, never()).save(any());
    }

    @Test
    void getModifierGroup_success() {
        when(modifierGroupRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(modifierGroup));

        ModifierGroupResponse response = modifierGroupService.getModifierGroup(1L, 5L);

        assertEquals(5L, response.id());
        assertEquals(1L, response.storeId());
        assertEquals("group", response.name());
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());

        verify(modifierGroupRepository).findByIdAndStoreId(5L, 1L);
    }

    @Test
    void getModifierGroup_throwsException_whenGroupNotFound() {
        when(modifierGroupRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierGroupNotFoundException.class,
                () -> modifierGroupService.getModifierGroup(1L, 5L));

        verify(modifierGroupRepository).findByIdAndStoreId(5L, 1L);
    }

    @Test
    void getModifierGroups_success() {
        when(modifierGroupRepository.findByStoreId(1L, pageable)).thenReturn(new PageImpl<>(List.of(modifierGroup)));

        Page<ModifierGroupResponse> page = modifierGroupService.getModifierGroups(1L, pageable);
        ModifierGroupResponse response = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(5L, response.id());
        assertEquals(1L, response.storeId());
        assertEquals("group", response.name());
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());

        verify(modifierGroupRepository).findByStoreId(1L, pageable);
    }

    @Test
    void getModifierGroups_returnsEmptyList_whenNoGroups() {
        when(modifierGroupRepository.findByStoreId(1L, pageable)).thenReturn(Page.empty());

        Page<ModifierGroupResponse> page = modifierGroupService.getModifierGroups(1L, pageable);

        assertTrue(page.isEmpty());
    }

    @Test
    void updateModifierGroup_success() {
        ModifierGroupRequest updateRequest = new ModifierGroupRequest(
                "group1",
                false,
                2,
                6
        );

        when(modifierGroupRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ModifierGroupResponse response = modifierGroupService.updateModifierGroup(1L, 5L, updateRequest);

        assertEquals(5L, response.id());
        assertEquals(1L, response.storeId());
        assertEquals("group1", response.name());
        assertFalse(response.required());
        assertEquals(2, response.minSelect());
        assertEquals(6, response.maxSelect());

        verify(modifierGroupRepository).save(modifierGroup);
    }

    @Test
    void updateModifierGroup_throwsException_GroupNotFound_() {
        ModifierGroupRequest updateRequest = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );

        when(modifierGroupRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.empty());


        assertThrows(ModifierGroupNotFoundException.class,
                () -> modifierGroupService.updateModifierGroup(1L, 5L, updateRequest));

        verify(modifierGroupRepository, never()).save(any());
    }

    @Test
    void updateModifierGroup_throwsException_whenQuantityIsInvalid() {
        ModifierGroupRequest updateRequest = new ModifierGroupRequest(
                "group",
                true,
                5,
                1
        );
        assertThrows(InvalidQuantityException.class,
                () -> modifierGroupService.updateModifierGroup(1L, 5L, updateRequest));

        verify(modifierGroupRepository, never()).save(any());
    }

    @Test
    void deleteModifierGroup_success() {
        when(modifierGroupRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.of(modifierGroup));

        modifierGroupService.deleteModifierGroup(1L, 5L);

        verify(modifierGroupRepository).delete(modifierGroup);
    }

    @Test
    void deleteModifierGroup_throwsException_whenGroupNotFound() {
        when(modifierGroupRepository.findByIdAndStoreId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierGroupNotFoundException.class,
                () -> modifierGroupService.deleteModifierGroup(1L, 5L));

        verify(modifierGroupRepository, never()).delete(any());
    }
}
