package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierOptionDto;
import com.delivery.habsida.dto.ModifierOptionRequest;
import com.delivery.habsida.exception.InvalidModifierOptionException;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.exception.ModifierOptionNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ModifierOptionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ModifierOptionController.class)
@Import(WebSecurityConfig.class)
public class ModifierOptionControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ModifierOptionService modifierOptionService;

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
    void createModifierOption_shouldReturn201() throws Exception {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(10),
                false
        );

        ModifierOptionDto response = new ModifierOptionDto(
                1L,
                1L,
                "option",
                new BigDecimal(10),
                false
        );
        when(modifierOptionService.createModifierOption(anyLong(), anyLong(), any())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/modifier-groups/{groupId}/options", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @WithMockUser
    @Test
    void createModifierOption_shouldReturn400_whenPriceIsFreeMismatch() throws Exception {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(10),
                true
        );

        when(modifierOptionService.createModifierOption(anyLong(), anyLong(), any())).thenThrow(new InvalidModifierOptionException("isFree must be false"));
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/modifier-groups/{groupId}/options", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void getModifierOption_shouldReturn200() throws Exception {
        ModifierOptionDto response = new ModifierOptionDto(
                1L,
                1L,
                "option",
                new BigDecimal(10),
                false
        );
        when(modifierOptionService.getModifierOption(anyLong(), anyLong(), anyLong())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 1L, 1L))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void getModifierOption_shouldReturn404_whenNotFound() throws Exception {
        when(modifierOptionService.getModifierOption(anyLong(), anyLong(), anyLong())).thenThrow(new ModifierOptionNotFoundException("Option not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 1L, 1L))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void getModifierOptions_shouldReturn200() throws Exception {
        ModifierOptionDto response = new ModifierOptionDto(
                1L,
                1L,
                "option",
                new BigDecimal(10),
                false
        );

        when(modifierOptionService.getModifierOptions(anyLong(), anyLong())).thenReturn(List.of(response));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}/options", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void getModifierOptions_shouldReturn404_whenGroupNotFound() throws Exception {
        when(modifierOptionService.getModifierOptions(anyLong(), anyLong())).thenThrow(new ModifierGroupNotFoundException("Group not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}/options", 1L, 1L))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void updateModifierOption_shouldReturn200() throws Exception {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(10),
                false
        );

        ModifierOptionDto response = new ModifierOptionDto(
                1L,
                1L,
                "option",
                new BigDecimal(10),
                false
        );

        when(modifierOptionService.updateModifierOption(anyLong(), anyLong(), anyLong(), any())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void updateModifierOption_shouldReturn404_whenNotFound() throws Exception {
        ModifierOptionRequest request = new ModifierOptionRequest(
                "option",
                new BigDecimal(10),
                false
        );

        when(modifierOptionService.updateModifierOption(anyLong(), anyLong(), anyLong(), any())).thenThrow(new ModifierOptionNotFoundException("Option not found"));
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void deleteModifierOption_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @WithMockUser
    @Test
    void deleteModifierOption_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ModifierOptionNotFoundException("Option not found"))
                .when(modifierOptionService).deleteModifierOption(anyLong(), anyLong(), anyLong());
        mockMvc.perform(delete("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
