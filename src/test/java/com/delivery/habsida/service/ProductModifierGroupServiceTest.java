package com.delivery.habsida.service;

import com.delivery.habsida.dto.ModifierGroupWithOptionsResponse;
import com.delivery.habsida.dto.ModifierOptionResponse;
import com.delivery.habsida.dto.ProductModifierGroupResponse;
import com.delivery.habsida.entity.ModifierGroup;
import com.delivery.habsida.entity.ModifierOption;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductModifierGroup;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.exception.ProductModifierGroupAlreadyExistsException;
import com.delivery.habsida.exception.ProductModifierGroupNotFoundException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.repository.ModifierGroupRepository;
import com.delivery.habsida.repository.ProductModifierGroupRepository;
import com.delivery.habsida.repository.ProductRepository;
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
public class ProductModifierGroupServiceTest {
    @Mock
    private ProductModifierGroupRepository productModifierGroupRepository;

    @Mock
    private ModifierGroupRepository modifierGroupRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductModifierGroupService productModifierGroupService;

    private ModifierGroup modifierGroup;
    private Product product;
    private ModifierOption modifierOption;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(2L);

        modifierGroup = new ModifierGroup();
        modifierGroup.setId(3L);
        modifierGroup.setName("group");
        modifierGroup.setRequired(true);
        modifierGroup.setMinSelect(1);
        modifierGroup.setMaxSelect(5);

        modifierOption = new ModifierOption();
        modifierOption.setId(4L);
        modifierOption.setName("option");
        modifierOption.setPriceDelta(new BigDecimal("10.2"));
        modifierOption.setModifierGroup(modifierGroup);

        modifierGroup.setModifierOptions(List.of(modifierOption));
    }

    private ProductModifierGroup defaultLink() {
        ProductModifierGroup productModifierGroup = new ProductModifierGroup();
        productModifierGroup.setId(5L);
        productModifierGroup.setProduct(product);
        productModifierGroup.setModifierGroup(modifierGroup);

        return productModifierGroup;
    }

    @Test
    void createProductModifierGroup_success() {
        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findByIdAndStoreId(3L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(productModifierGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductModifierGroupResponse response = productModifierGroupService.createProductModifierGroup(1L, 2L, 3L);

        assertEquals(2L, response.productId());
        assertEquals(3L, response.modifierGroupId());

        verify(productModifierGroupRepository).save(any());
    }

    @Test
    void createProductModifierGroup_throwsException_whenAlreadyExists() {
        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findByIdAndStoreId(3L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(2L, 3L)).thenReturn(true);

        assertThrows(ProductModifierGroupAlreadyExistsException.class,
                () -> productModifierGroupService.createProductModifierGroup(1L, 2L, 3L));

        verify(productModifierGroupRepository, never()).save(any());
    }

    @Test
    void getModifierGroupWithOptions_success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findModifierGroupsByProductId(2L, pageable)).thenReturn(new PageImpl<>(List.of(modifierGroup)));

        Page<ModifierGroupWithOptionsResponse> page = productModifierGroupService.getModifierGroupWithOptions(1L, 2L, pageable);

        ModifierGroupWithOptionsResponse response = page.getContent().get(0);
        assertEquals(1, page.getContent().size());
        assertEquals(3L, response.id());
        assertEquals("group", response.name());
        assertTrue(response.required());
        assertEquals(1, response.minSelect());
        assertEquals(5, response.maxSelect());

        ModifierOptionResponse option = response.options().get(0);
        assertEquals(4L, option.id());
        assertEquals(3L, option.modifierGroupId());
        assertEquals("option", option.name());
        assertEquals(new BigDecimal("10.2"), option.priceDelta());
        assertFalse(option.isFree());

        verify(modifierGroupRepository).findModifierGroupsByProductId(2L, pageable);
    }

    @Test
    void getModifierGroupWithOptions_throwsException_whenProductNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productModifierGroupService.getModifierGroupWithOptions(1L, 2L, pageable));

        verify(productModifierGroupRepository, never()).findByProductId(anyLong(), any(Pageable.class));
    }

    @Test
    void deleteProductModifierGroup_success() {
        ProductModifierGroup deleteLink = defaultLink();

        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findByIdAndStoreId(3L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(productModifierGroupRepository.findByProductIdAndModifierGroupId(2L, 3L)).thenReturn(Optional.of(deleteLink));

        productModifierGroupService.deleteProductModifierGroup(1L, 2L, 3L);
        verify(productModifierGroupRepository).delete(deleteLink);
    }

    @Test
    void deleteProductModifierGroup_throwsException_whenGroupNotFound() {
        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findByIdAndStoreId(3L, 1L)).thenReturn(Optional.empty());

        assertThrows(ModifierGroupNotFoundException.class,
                () -> productModifierGroupService.deleteProductModifierGroup(1L, 2L, 3L));

        verify(productModifierGroupRepository, never()).delete(any());
    }

    @Test
    void deleteProductModifierGroup_throwsException_whenLinkNotFound() {
        when(productRepository.findByIdAndStoreId(2L, 1L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findByIdAndStoreId(3L, 1L)).thenReturn(Optional.of(modifierGroup));
        when(productModifierGroupRepository.findByProductIdAndModifierGroupId(2L, 3L)).thenReturn(Optional.empty());

        assertThrows(ProductModifierGroupNotFoundException.class,
                () -> productModifierGroupService.deleteProductModifierGroup(1L, 2L, 3L));

        verify(productModifierGroupRepository, never()).delete(any());
    }
}
