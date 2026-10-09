package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupDto;
import com.delivery.habsida.dto.ModifierGroupRequest;
import com.delivery.habsida.exception.InvalidQuantityException;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ModifierGroupService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModifierGroupController.class)
@Import(WebSecurityConfig.class)

public class ModifierGroupControllerTest {
    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ModifierGroupService modifierGroupService;

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
    void createModifierGroup_success() throws Exception {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );
        ModifierGroupDto response = new ModifierGroupDto(
                1L,
                1L,
                "group",
                true,
                1,
                5
        );
        when(modifierGroupService.createModifierGroup(anyLong(), any())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/modifier-groups", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @WithMockUser
    @Test
    void createModifierGroup_invalidQuantity_shouldReturn400() throws Exception {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                5,
                1
        );
        when(modifierGroupService.createModifierGroup(anyLong(), any())).thenThrow(new InvalidQuantityException("minSelect cannot be greater than maxSelect"));
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/modifier-groups", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").value("minSelect cannot be greater than maxSelect"))
                .andExpect(jsonPath("$.path").value("/stores/1/modifier-groups"));
    }

    @WithMockUser
    @Test
    void getModifierGroups_success() throws Exception {
        ModifierGroupDto response = new ModifierGroupDto(
                1L,
                1L,
                "group",
                true,
                1,
                5
        );
        when(modifierGroupService.getModifierGroups(anyLong())).thenReturn(List.of(response));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(1L))
                .andExpect(jsonPath("$[0].storeId").value(1L))
                .andExpect(jsonPath("$[0].name").value("group"))
                .andExpect(jsonPath("$[0].required").value(true))
                .andExpect(jsonPath("$[0].minSelect").value(1))
                .andExpect(jsonPath("$[0].maxSelect").value(5));
    }

    @WithMockUser
    @Test
    void getModifierGroups_shouldReturn404_whenGroupNotFound() throws Exception {
        when(modifierGroupService.getModifierGroups(anyLong())).thenThrow(new ModifierGroupNotFoundException("Group not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value("Group not found"))
                .andExpect(jsonPath("$.path").value("/stores/1/modifier-groups"));
    }

    @WithMockUser
    @Test
    void getModifierGroup_success() throws Exception {
        ModifierGroupDto response = new ModifierGroupDto(
                1L,
                1L,
                "group",
                true,
                1,
                5
        );
        when(modifierGroupService.getModifierGroup(anyLong(), anyLong())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.name").value("group"))
                .andExpect(jsonPath("$.required").value(true))
                .andExpect(jsonPath("$.minSelect").value(1))
                .andExpect(jsonPath("$.maxSelect").value(5));
    }

    @WithMockUser
    @Test
    void getModifierGroup_shouldReturn404_whenNotFound() throws Exception {
        when(modifierGroupService.getModifierGroup(anyLong(), anyLong())).thenThrow(new ModifierGroupNotFoundException("Group not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}", 1L, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value("Group not found"))
                .andExpect(jsonPath("$.path").value("/stores/1/modifier-groups/1"));
    }

    @WithMockUser
    @Test
    void updateModifierGroup_success() throws Exception {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );
        ModifierGroupDto response = new ModifierGroupDto(
                1L,
                1L,
                "group",
                true,
                1,
                5
        );
        when(modifierGroupService.updateModifierGroup(anyLong(), anyLong(), any())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/modifier-groups/{groupId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.name").value("group"))
                .andExpect(jsonPath("$.required").value(true))
                .andExpect(jsonPath("$.minSelect").value(1))
                .andExpect(jsonPath("$.maxSelect").value(5));
    }

    @WithMockUser
    @Test
    void updateModifierGroup_shouldReturn404_whenGroupNotFound() throws Exception {
        ModifierGroupRequest request = new ModifierGroupRequest(
                "group",
                true,
                1,
                5
        );
        when(modifierGroupService.updateModifierGroup(anyLong(), anyLong(), any())).thenThrow(new ModifierGroupNotFoundException("Group not found"));
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/modifier-groups/{groupId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value("Group not found"))
                .andExpect(jsonPath("$.path").value("/stores/1/modifier-groups/1"));
    }

    @WithMockUser
    @Test
    void deleteModifierGroup_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/modifier-groups/{groupId}", 1L, 1L))
                .andExpect(status().isNoContent());
        verify(modifierGroupService).deleteModifierGroup(1L, 1L);
    }

    @WithMockUser
    @Test
    void deleteModifierGroup_shouldReturn404_whenGroupNotFound() throws Exception {
        doThrow(new ModifierGroupNotFoundException("group not found"))
                .when(modifierGroupService).deleteModifierGroup(anyLong(), anyLong());
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/modifier-groups/{groupId}", 1L, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value("group not found"))
                .andExpect(jsonPath("$.path").value("/stores/1/modifier-groups/1"));
        verify(modifierGroupService).deleteModifierGroup(1L, 1L);
    }
}
