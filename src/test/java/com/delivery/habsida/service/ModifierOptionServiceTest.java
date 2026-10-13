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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModifierOptionServiceTest {
    @Mock
    private ModifierOptionRepository modifierOptionRepository;

    @Mock
    private ModifierGroupRepository modifierGroupRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    @InjectMocks
    private ModifierOptionService modifierOptionService;

    private ModifierGroup modifierGroup;

    @BeforeEach
    void setUp() {
        modifierGroup = new ModifierGroup();
        modifierGroup.setId(2L);
    }

    private ModifierOptionRequest defaultRequest() {
        return new ModifierOptionRequest(
                "name",
                (new BigDecimal("10.00")),
                false
        );
    }

    private ModifierOption defaultOption() {
        ModifierOption option = new ModifierOption();
        option.setId(3L);
        option.setName("name");
        option.setPriceDelta(new BigDecimal("10.00"));
        option.setIsFree(false);
        option.setModifierGroup(modifierGroup);

        return option;
    }

    @Test
    void createModifierOption_success() {
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ModifierOptionResponse response = modifierOptionService.createModifierOption(1L, 2L, defaultRequest());

        assertEquals("name", response.name());
        assertEquals(new BigDecimal("10.00"), response.priceDelta());
        assertFalse(response.isFree());
        assertEquals(2L, response.modifierGroupId());

        verify(modifierOptionRepository).save(any());
    }

    @Test
    void createModifierOption_throwsException_whenPriceIsFree() {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "name",
                new BigDecimal("0.00"),
                false
        );
        assertThrows(InvalidModifierOptionException.class,
                () -> modifierOptionService.createModifierOption(1L, 2L, request));

        verify(modifierOptionRepository, never()).save(any());
    }

    @Test
    void getModifierOption_success() {
        when(modifierOptionRepository.findByIdAndModifierGroupId(3L, 2L)).thenReturn(Optional.of(defaultOption()));
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));

        ModifierOptionResponse response = modifierOptionService.getModifierOption(1L, 2L, 3L);

        assertEquals(3L, response.id());
        assertEquals("name", response.name());
        assertEquals(new BigDecimal("10.00"), response.priceDelta());
        assertFalse(response.isFree());
        assertEquals(2L, response.modifierGroupId());

        verify(modifierOptionRepository).findByIdAndModifierGroupId(3L, 2L);
    }

    @Test
    void getModifierOption_throwsException_whenOptionNotFound() {
        when(modifierOptionRepository.findByIdAndModifierGroupId(1L, 1L)).thenReturn(Optional.empty());
        when(modifierGroupRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(modifierGroup));

        assertThrows(ModifierOptionNotFoundException.class,
                () -> modifierOptionService.getModifierOption(1L, 1L, 1L));
    }

    @Test
    void getModifierOptions_success() {
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.findByModifierGroupIdOrderByIdAsc(2L, pageable)).thenReturn(new PageImpl<>(List.of(defaultOption())));

        Page<ModifierOptionResponse> page = modifierOptionService.getModifierOptions(1L, 2L, pageable);
        ModifierOptionResponse response = page.getContent().get(0);
        assertEquals(1, page.getContent().size());
        assertEquals(3L, response.id());
        assertEquals("name", response.name());
        assertEquals(new BigDecimal("10.00"), response.priceDelta());
        assertFalse(response.isFree());
        assertEquals(2L, response.modifierGroupId());

        verify(modifierOptionRepository).findByModifierGroupIdOrderByIdAsc(2L, pageable);
    }

    @Test
    void getModifierOptions_throwsException_whenGroupNotFound() {
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierGroupNotFoundException.class,
                () -> modifierOptionService.getModifierOptions(1L, 2L, pageable));
    }

    @Test
    void updateModifierOption_success() {
        ModifierOptionRequest updateRequest = new ModifierOptionRequest(
                "name1",
                new BigDecimal("0.00"),
                true
        );

        ModifierOption updateOption = defaultOption();

        when(modifierOptionRepository.findByIdAndModifierGroupId(3L, 2L)).thenReturn(Optional.of(updateOption));
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ModifierOptionResponse response = modifierOptionService.updateModifierOption(1L, 2L, 3L, updateRequest);

        assertEquals(3L, response.id());
        assertEquals("name1", response.name());
        assertEquals(new BigDecimal("0.00"), response.priceDelta());
        assertTrue(response.isFree());
        assertEquals(2L, response.modifierGroupId());

        verify(modifierOptionRepository).save(updateOption);
    }

    @Test
    void updateModifierOption_throwsException_whenOptionNotFound() {
        ModifierOptionRequest updateRequest = new ModifierOptionRequest(
                "option",
                new BigDecimal(0),
                true
        );

        when(modifierOptionRepository.findByIdAndModifierGroupId(3L, 2L)).thenReturn(Optional.empty());
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));

        assertThrows(ModifierOptionNotFoundException.class,
                () -> modifierOptionService.updateModifierOption(1L, 2L, 3L, updateRequest));

        verify(modifierOptionRepository, never()).save(any());
    }

    @Test
    void updateModifierOption_shouldThrowsExceptions_whenPriceIsFreeMismatch() {
        ModifierOptionRequest updateRequest = new ModifierOptionRequest(
                "option",
                new BigDecimal("0.00"),
                false
        );

        assertThrows(InvalidModifierOptionException.class,
                () -> modifierOptionService.updateModifierOption(1L, 2L, 3L, updateRequest));

        verify(modifierOptionRepository, never()).save(any());
    }

    @Test
    void deleteModifierOption_success() {
        ModifierOption deleteOption = defaultOption();

        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.findByIdAndModifierGroupId(3L, 2L)).thenReturn(Optional.of(deleteOption));

        modifierOptionService.deleteModifierOption(1L, 2L, 3L);

        verify(modifierOptionRepository).delete(deleteOption);
    }

    @Test
    void deleteModifierOption_shouldThrowsException_whenOptionNotFound() {
        when(modifierGroupRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(modifierOptionRepository.findByIdAndModifierGroupId(3L, 2L)).thenReturn(Optional.empty());

        assertThrows(ModifierOptionNotFoundException.class,
                () -> modifierOptionService.deleteModifierOption(1L, 2L, 3L));

        verify(modifierOptionRepository, never()).delete(any());
    }
}
