package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreDeliveryAreaResponse;
import com.delivery.habsida.dto.StoreDeliveryRestrictionRequest;
import com.delivery.habsida.dto.StoreDeliveryRestrictionResponse;
import com.delivery.habsida.entity.StoreDeliveryRestriction;
import com.delivery.habsida.exception.StoreDeliveryRestrictionAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliveryRestrictionNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.StoreDeliveryAreaService;
import com.delivery.habsida.service.StoreDeliveryRestrictionService;
import com.delivery.habsida.service.StoreDeliveryRestrictionServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.web.servlet.function.ServerResponse.status;

@WebMvcTest(StoreDeliveryRestrictionController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class StoreDeliveryRestrictionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private StoreDeliveryRestrictionService storeDeliveryRestrictionService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    private StoreDeliveryRestrictionRequest defaultRequest() {
        return new StoreDeliveryRestrictionRequest(
                "Seoul"
        );
    }

    private StoreDeliveryRestrictionResponse defaultResponse() {
        return new StoreDeliveryRestrictionResponse(
                5L,
                1L,
                "Seoul"
        );
    }

    @Test
    void createStoreDeliveryRestriction_success() throws Exception {
        when(storeDeliveryRestrictionService.createStoreDeliveryRestriction(anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/delivery-restrictions", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.restrictedArea").value("Seoul"));

        verify(storeDeliveryRestrictionService).createStoreDeliveryRestriction(anyLong(), any());
    }

    @Test
    void createStoreDeliveryRestriction_shouldReturnConflict_whenRestrictionAlreadyExists() throws Exception {
        when(storeDeliveryRestrictionService.createStoreDeliveryRestriction(anyLong(), any())).thenThrow(new StoreDeliveryRestrictionAlreadyExistsException("Restricted area already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/delivery-restrictions", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isConflict());

        verify(storeDeliveryRestrictionService).createStoreDeliveryRestriction(anyLong(), any());
    }

    @Test
    void getStoreDeliveryRestrictions_success() throws Exception {
        when(storeDeliveryRestrictionService.getStoreDeliveryRestrictions(anyLong())).thenReturn(List.of(defaultResponse()));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-restrictions", 1L))
                .andExpect(status().isOk());

        verify(storeDeliveryRestrictionService).getStoreDeliveryRestrictions(anyLong());
    }

    @Test
    void getStoreDeliveryRestrictions_shouldReturnNotFound_whenStoreNotFound() throws Exception {
        when(storeDeliveryRestrictionService.getStoreDeliveryRestrictions(anyLong())).thenThrow(new StoreNotFoundException("Store not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-restrictions", 1L))
                .andExpect(status().isNotFound());

        verify(storeDeliveryRestrictionService).getStoreDeliveryRestrictions(anyLong());
    }

    @Test
    void getStoreDeliveryRestriction_success() throws Exception {
        when(storeDeliveryRestrictionService.getStoreDeliveryRestriction(anyLong(), anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-restrictions/{restrictionId}", 1L, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.restrictedArea").value("Seoul"));

        verify(storeDeliveryRestrictionService).getStoreDeliveryRestriction(anyLong(), anyLong());
    }

    @Test
    void getStoreDeliveryRestriction_shouldReturnNotFound_whenRestrictionNotFound() throws Exception {
        when(storeDeliveryRestrictionService.getStoreDeliveryRestriction(anyLong(), anyLong())).thenThrow(new StoreDeliveryRestrictionNotFoundException("Restriction not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-restrictions/{restrictionId}", 1L, 5L))
                .andExpect(status().isNotFound());

        verify(storeDeliveryRestrictionService).getStoreDeliveryRestriction(anyLong(), anyLong());
    }

    @Test
    void updateStoreDeliveryRestriction_success() throws Exception {
        StoreDeliveryRestrictionResponse updateResponse = new StoreDeliveryRestrictionResponse(
                55L,
                11L,
                "Cheonan"
        );

        when(storeDeliveryRestrictionService.updateStoreDeliveryRestriction(anyLong(), anyLong(), any())).thenReturn(updateResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/delivery-restrictions/{restrictionId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55L))
                .andExpect(jsonPath("$.storeId").value(11L))
                .andExpect(jsonPath("$.restrictedArea").value("Cheonan"));

        verify(storeDeliveryRestrictionService).updateStoreDeliveryRestriction(anyLong(), any(), any());
    }

    @Test
    void updateStoreDeliveryRestriction_shouldReturnConflict_whenRestrictedAreaAlreadyExists() throws Exception {
        when(storeDeliveryRestrictionService.updateStoreDeliveryRestriction(anyLong(), anyLong(), any())).thenThrow(new StoreDeliveryRestrictionAlreadyExistsException("Restriction already exists"));

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/delivery-restrictions/{restrictionId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isConflict());

        verify(storeDeliveryRestrictionService).updateStoreDeliveryRestriction(anyLong(), anyLong(), any());
    }

    @Test
    void deleteStoreStoreDeliveryRestriction_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/delivery-restrictions/{restrictionId}", 1L, 5L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStoreStoreDeliveryRestriction_shouldReturnNotFound_whenStoreNotFound() throws Exception {
        doThrow(new StoreNotFoundException("Store not found"))
                .when(storeDeliveryRestrictionService).deleteStoreDeliveryRestriction(anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/delivery-restrictions/{restrictionId}", 1L, 5L))
                .andExpect(status().isNotFound());
    }
}