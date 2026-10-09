package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupWithOptionsDto;
import com.delivery.habsida.dto.ModifierOptionDto;
import com.delivery.habsida.dto.ProductModifierGroupDto;
import com.delivery.habsida.exception.ProductModifierGroupAlreadyExistsException;
import com.delivery.habsida.exception.ProductModifierGroupNotFoundException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ProductModifierGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductModifierGroupController.class)
@Import(WebSecurityConfig.class)

public class ProductModifierGroupControllerTest {
    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductModifierGroupService productModifierGroupService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    @WithMockUser
    @Test
    void createProductModifierGroup_success() throws Exception {
        ProductModifierGroupDto dto = new ProductModifierGroupDto(
                1L,
                1L,
                1L
        );
        when(productModifierGroupService.createProductModifierGroup(anyLong(), anyLong(), anyLong())).thenReturn(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @WithMockUser
    @Test
    void createProductModifierGroup_shouldReturn409_whenDuplicate() throws Exception {
        when(productModifierGroupService.createProductModifierGroup(anyLong(), anyLong(), anyLong())).thenThrow(new ProductModifierGroupAlreadyExistsException(" ProductModifierGroup already exists"));
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @WithMockUser
    @Test
    void getModifierGroupWithOptions_success() throws Exception {
        ModifierGroupWithOptionsDto dto = new ModifierGroupWithOptionsDto(
                1L,
                "name",
                true,
                1,
                2,
                List.of(
                        new ModifierOptionDto(
                                1L,
                                1L,
                                "name",
                                new BigDecimal("10.2"),
                                false
                        )
                )
        );
        when(productModifierGroupService.getModifierGroupWithOptions(anyLong(), anyLong())).thenReturn(List.of(dto));
             mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/products/{productId}/modifier-groups", 1L, 1L)
                     .contentType(MediaType.APPLICATION_JSON))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$", hasSize(1)))
                     .andExpect(jsonPath("$[0].id").value(1L))
                     .andExpect(jsonPath("$[0].name").value("name"))
                     .andExpect(jsonPath("$[0].required").value(true))
                     .andExpect(jsonPath("$[0].minSelect").value(1))
                     .andExpect(jsonPath("$[0].maxSelect").value(2));
    }

    @WithMockUser
    @Test
    void getModifierGroupWithOptions_shouldReturn404_whenNotFound() throws Exception {
        when(productModifierGroupService.getModifierGroupWithOptions(anyLong(), anyLong())).thenThrow(new ProductNotFoundException("Product not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/products/{productId}/modifier-groups", 1L, 1L))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void deleteProductModifierGroup_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @WithMockUser
    @Test
    void deleteProductModifierGroup_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ProductModifierGroupNotFoundException("Group not found"))
                .when(productModifierGroupService).deleteProductModifierGroup(anyLong(), anyLong(), anyLong());
        mockMvc.perform(delete("/stores/{storeId}/products/{productId}/modifier-groups/{groupId}", 1L, 1L, 1L))
                .andExpect(status().isNotFound());
    }
}
