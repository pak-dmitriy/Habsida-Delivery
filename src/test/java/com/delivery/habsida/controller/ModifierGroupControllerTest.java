package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ModifierGroupRequest;
import com.delivery.habsida.dto.ModifierGroupResponse;
import com.delivery.habsida.exception.ModifierGroupNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ModifierGroupService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModifierGroupController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class ModifierGroupControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ModifierGroupService modifierGroupService;

    @MockitoBean
    private JwtService jwtService;

    private ModifierGroupResponse defaultResponse() {
        return new ModifierGroupResponse(
                5L,
                1L,
                "name",
                true,
                1,
                5
        );
    }

    private ModifierGroupRequest defaultRequest() {
        return new ModifierGroupRequest(
                "name",
                true,
                1,
                5
        );
    }

    @Test
    void createModifierGroup_success() throws Exception {
        when(modifierGroupService.createModifierGroup(anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/modifier-groups", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(5L))
                        .andExpect(jsonPath("$.storeId").value(1L))
                        .andExpect(jsonPath("$.name").value("name"))
                        .andExpect(jsonPath("$.required").value(true))
                        .andExpect(jsonPath("$.minSelect").value(1))
                        .andExpect(jsonPath("$.maxSelect").value(5));

        verify(modifierGroupService).createModifierGroup(eq(1L), any());
    }

    @Test
    void getModifierGroups_success() throws Exception {
        when(modifierGroupService.getModifierGroups(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[0].storeId").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("name"))
                .andExpect(jsonPath("$.content[0].required").value(true))
                .andExpect(jsonPath("$.content[0].minSelect").value(1))
                .andExpect(jsonPath("$.content[0].maxSelect").value(5));

        verify(modifierGroupService).getModifierGroups(eq(1L), any(Pageable.class));
    }

    @Test
    void getModifierGroup_success() throws Exception {
        when(modifierGroupService.getModifierGroup(anyLong(), anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/modifier-groups/{groupId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.required").value(true))
                .andExpect(jsonPath("$.minSelect").value(1))
                .andExpect(jsonPath("$.maxSelect").value(5));

        verify(modifierGroupService).getModifierGroup(1L, 5L);
    }

    @Test
    void updateModifierGroup_success() throws Exception {
        ModifierGroupResponse updateResponse = defaultResponse();

        when(modifierGroupService.updateModifierGroup(anyLong(), anyLong(), any())).thenReturn(updateResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/modifier-groups/{groupId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.required").value(true))
                .andExpect(jsonPath("$.minSelect").value(1))
                .andExpect(jsonPath("$.maxSelect").value(5));

        verify(modifierGroupService).updateModifierGroup(eq(1L), eq(5L), any());
    }

    @Test
    void deleteModifierGroup_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/modifier-groups/{groupId}", 1L, 5L))
                .andExpect(status().isNoContent());

        verify(modifierGroupService).deleteModifierGroup(1L, 5L);
    }

    @Test
    void deleteModifierGroup_shouldReturn404_whenGroupNotFound() throws Exception {
        doThrow(new ModifierGroupNotFoundException("Group not found"))
                .when(modifierGroupService).deleteModifierGroup(anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/modifier-groups/{groupId}", 1L, 5L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value("Group not found"))
                .andExpect(jsonPath("$.path").value("/stores/1/modifier-groups/5"));

        verify(modifierGroupService).deleteModifierGroup(1L, 5L);
    }
}
