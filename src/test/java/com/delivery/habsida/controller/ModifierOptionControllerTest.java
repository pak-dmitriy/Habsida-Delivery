package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierOptionRequest;
import com.delivery.habsida.dto.ModifierOptionResponse;
import com.delivery.habsida.exception.ModifierOptionNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ModifierOptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModifierOptionController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class ModifierOptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModifierOptionService modifierOptionService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ModifierOptionRequest defaultRequest() {
        return new ModifierOptionRequest(
                "name",
                new BigDecimal("10.00"),
                false
        );
    }

    private ModifierOptionResponse defaultResponse() {
        return new ModifierOptionResponse(
                3L,
                2L,
                "name",
                new BigDecimal("10.00"),
                false
        );
    }

    @Test
    void createModifierOption_shouldReturn201() throws Exception {
        when(modifierOptionService.createModifierOption(anyLong(), anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/modifier-groups/{groupId}/options", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.modifierGroupId").value(2L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.priceDelta").value(10.00))
                .andExpect(jsonPath("$.isFree").value(false));

        verify(modifierOptionService).createModifierOption(eq(1L), eq(2L), any());
    }

    @Test
    void getModifierOption_shouldReturn200() throws Exception {
        when(modifierOptionService.getModifierOption(anyLong(), anyLong(), anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 2L, 3L))
                .andExpect(status().isOk());

        verify(modifierOptionService).getModifierOption(1L, 2L, 3L);
    }

    @Test
    void getModifierOptions_shouldReturn200() throws Exception {
        when(modifierOptionService.getModifierOptions(anyLong(), anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}/options", 1L, 2L))
                .andExpect(status().isOk());

        verify(modifierOptionService).getModifierOptions(eq(1L), eq(2L), any(Pageable.class));
    }

    @Test
    void updateModifierOption_shouldReturn200() throws Exception {
        ModifierOptionResponse response = defaultResponse();

        when(modifierOptionService.updateModifierOption(anyLong(), anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 2L, 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk());

        verify(modifierOptionService).updateModifierOption(eq(1L), eq(2L), eq(3L), any());
    }

    @Test
    void deleteModifierOption_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 2L, 3L))
                .andExpect(status().isNoContent());

        verify(modifierOptionService).deleteModifierOption(1L, 2L, 3L);
    }

    @Test
    void deleteModifierOption_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ModifierOptionNotFoundException("Option not found"))
                .when(modifierOptionService).deleteModifierOption(anyLong(), anyLong(), anyLong());

        mockMvc.perform(delete("/stores/{storeId}/modifier-groups/{groupId}/options/{optionId}", 1L, 2L, 3L))
                .andExpect(status().isNotFound());
    }
}
