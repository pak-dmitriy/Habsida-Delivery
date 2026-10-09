package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreDeliveryAreaRequest;
import com.delivery.habsida.dto.StoreDeliveryAreaResponse;
import com.delivery.habsida.exception.StoreDeliveryAreaAlreadyExistsException;
import com.delivery.habsida.exception.StoreDeliveryAreaNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.StoreDeliveryAreaService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreDeliveryAreaController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class StoreDeliveryAreaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StoreDeliveryAreaService storeDeliveryAreaService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    private StoreDeliveryAreaRequest defaultRequest() {
        return new StoreDeliveryAreaRequest(
                "Asan",
                new BigDecimal("10.00")
        );
    }

    private StoreDeliveryAreaResponse defaultResponse() {
        return new StoreDeliveryAreaResponse(
                5L,
                1L,
                "Asan",
                new BigDecimal("10.00")
        );
    }

    @Test
    void createStoreDeliveryArea_success() throws Exception {
        when(storeDeliveryAreaService.createStoreDeliveryArea(any(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/delivery-areas", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.districtName").value("Asan"))
                .andExpect(jsonPath("$.deliveryFee").value(10.00));

        verify(storeDeliveryAreaService).createStoreDeliveryArea(anyLong(), any());
    }

    @Test
    void createStoreDeliveryArea_shouldReturnConflict_whenAreaAlreadyExist() throws Exception {
        when(storeDeliveryAreaService.createStoreDeliveryArea(any(), any())).thenThrow(new StoreDeliveryAreaAlreadyExistsException("Area already exist"));

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/delivery-areas", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isConflict());

        verify(storeDeliveryAreaService).createStoreDeliveryArea(anyLong(), any());
    }

    @Test
    void getStoreDeliveryAreas_success() throws Exception {
        when(storeDeliveryAreaService.getStoreDeliveryAreas(any())).thenReturn(List.of(defaultResponse()));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-areas", 1L))
                .andExpect(status().isOk());

        verify(storeDeliveryAreaService).getStoreDeliveryAreas(1L);
    }

    @Test
    void getStoreDeliveryAreas_shouldReturnNotFound_whenStoreOrAreasNotFound() throws Exception {
        when(storeDeliveryAreaService.getStoreDeliveryAreas(any())).thenThrow(new StoreDeliveryAreaNotFoundException("Area not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-areas", 1L))
                .andExpect(status().isNotFound());

        verify(storeDeliveryAreaService).getStoreDeliveryAreas(1L);
    }

    @Test
    void getStoreDeliveryArea_success() throws Exception {
        when(storeDeliveryAreaService.getStoreDeliveryArea(anyLong(), anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-areas/{areaId}", 1L, 5L))
                .andExpect(status().isOk());

        verify(storeDeliveryAreaService).getStoreDeliveryArea(1L, 5L);
    }

    @Test
    void getStoreDeliveryArea_shouldReturnNotFound_whenStoreOrAreasNotFound() throws Exception {
        when(storeDeliveryAreaService.getStoreDeliveryArea(anyLong(), anyLong())).thenThrow(new StoreNotFoundException("Store not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-areas/{areaId}", 1L, 5L))
                .andExpect(status().isNotFound());

        verify(storeDeliveryAreaService).getStoreDeliveryArea(anyLong(), anyLong());
    }

    @Test
    void updateStoreDeliveryArea_success() throws Exception {
        StoreDeliveryAreaResponse updatedResponse = new StoreDeliveryAreaResponse(
                6L,
                2L,
                "Cheonan",
                new BigDecimal("20.00")
        );

        when(storeDeliveryAreaService.updateStoreDeliveryArea(anyLong(), anyLong(), any())).thenReturn(updatedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/delivery-areas/{areaId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.districtName").value("Cheonan"))
                .andExpect(jsonPath("$.deliveryFee").value(20.00));

        verify(storeDeliveryAreaService).updateStoreDeliveryArea(anyLong(), anyLong(), any());
    }

    @Test
    void updateStoreDeliveryArea_shouldReturnConflict_whenStoreOrAreaAlreadyExist() throws Exception {
        when(storeDeliveryAreaService.updateStoreDeliveryArea(anyLong(), anyLong(), any())).thenThrow(new StoreDeliveryAreaAlreadyExistsException("Area already exist"));

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/delivery-areas/{areaId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isConflict());

        verify(storeDeliveryAreaService).updateStoreDeliveryArea(anyLong(), anyLong(), any());
    }

    @Test
    void deleteStoreDeliveryArea_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/delivery-areas/{areaId}", 1L, 5L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStoreDeliveryArea_shouldReturn404_whenStoreNotFound() throws Exception {
        doThrow(new StoreNotFoundException("Store not found"))
                .when(storeDeliveryAreaService).deleteStoreDeliveryArea(anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/delivery-areas/{areaId}", 1L, 5L))
                .andExpect(status().isNotFound());
    }
}
