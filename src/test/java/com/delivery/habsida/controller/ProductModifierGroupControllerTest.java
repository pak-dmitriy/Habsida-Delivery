package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupWithOptionsResponse;
import com.delivery.habsida.dto.ModifierOptionResponse;
import com.delivery.habsida.dto.ProductModifierGroupResponse;
import com.delivery.habsida.exception.ProductModifierGroupNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ProductModifierGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductModifierGroupController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class ProductModifierGroupControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductModifierGroupService productModifierGroupService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createProductModifierGroup_success() throws Exception {
        ProductModifierGroupResponse response = new ProductModifierGroupResponse(
                5L,
                2L,
                3L
        );
        when(productModifierGroupService.createProductModifierGroup(anyLong(), anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 2L, 3L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.productId").value(2L))
                .andExpect(jsonPath("$.modifierGroupId").value(3L));

        verify(productModifierGroupService).createProductModifierGroup(1L, 2L, 3L);
    }

    @Test
    void getModifierGroupWithOptions_success() throws Exception {
        ModifierGroupWithOptionsResponse response = new ModifierGroupWithOptionsResponse(
                6L,
                "with",
                true,
                1,
                5,
                List.of(
                        new ModifierOptionResponse(
                                7L,
                                6L,
                                "option",
                                new BigDecimal("10.2"),
                                false
                        )
                )
        );
        when(productModifierGroupService.getModifierGroupWithOptions(
                anyLong(),
                anyLong(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/products/{productId}/modifier-groups", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(6L))
                .andExpect(jsonPath("$.content[0].name").value("with"))
                .andExpect(jsonPath("$.content[0].required").value(true))
                .andExpect(jsonPath("$.content[0].minSelect").value(1))
                .andExpect(jsonPath("$.content[0].maxSelect").value(5))

                .andExpect(jsonPath("$.content[0].options", hasSize(1)))
                .andExpect(jsonPath("$.content[0].options[0].id").value(7L))
                .andExpect(jsonPath("$.content[0].options[0].modifierGroupId").value(6L))
                .andExpect(jsonPath("$.content[0].options[0].name").value("option"))
                .andExpect(jsonPath("$.content[0].options[0].priceDelta").value(10.2))
                .andExpect(jsonPath("$.content[0].options[0].isFree").value(false));

        verify(productModifierGroupService).getModifierGroupWithOptions(eq(1L), eq(2L), any(Pageable.class));
    }

    @Test
    void deleteProductModifierGroup_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 2L, 3L))
                .andExpect(status().isNoContent());

        verify(productModifierGroupService).deleteProductModifierGroup(1L, 2L, 3L);
    }

    @Test
    void deleteProductModifierGroup_shouldReturn404_whenGroupNotFound() throws Exception {
        doThrow(new ProductModifierGroupNotFoundException("Group not found"))
                .when(productModifierGroupService).deleteProductModifierGroup(anyLong(), anyLong(), anyLong());

        mockMvc.perform(delete("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 2L, 3L))
                .andExpect(status().isNotFound());

        verify(productModifierGroupService).deleteProductModifierGroup(1L, 2L, 3L);
    }
}
