package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreDeliverySettingRequest;
import com.delivery.habsida.dto.StoreDeliverySettingResponse;
import com.delivery.habsida.entity.DeliveryGeography;
import com.delivery.habsida.entity.DeliveryType;
import com.delivery.habsida.exception.StoreDeliverySettingNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.StoreDeliverySettingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreDeliverySettingController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class StoreDeliverySettingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StoreDeliverySettingService storeDeliverySettingService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    private StoreDeliverySettingRequest defaultRequest() {
        return new StoreDeliverySettingRequest(
                true,
                DeliveryType.SAME_DAY,
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                DeliveryGeography.ONLY_IN_THE_CITY
        );
    }

    @Test
    void createStoreDeliverySetting_success() throws Exception {
        StoreDeliverySettingResponse response = new StoreDeliverySettingResponse(
                1L,
                1L,
                true,
                DeliveryType.SAME_DAY,
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                DeliveryGeography.ONLY_IN_THE_CITY
        );

        when(storeDeliverySettingService.createStoreDeliverySetting(anyLong(), any())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/delivery-settings", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.deliveryType").value("SAME_DAY"))
                .andExpect(jsonPath("$.minOrderPrice").value(10.00))
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(10.00))
                .andExpect(jsonPath("$.maxDistance").value(10.00))
                .andExpect(jsonPath("$.deliveryGeography").value("ONLY_IN_THE_CITY"));

        verify(storeDeliverySettingService).createStoreDeliverySetting(anyLong(), any());
    }

    @Test
    void createStoreDeliverySetting__shouldReturnBadRequest_whenMinOrderPriceIsNegative() throws Exception {
        StoreDeliverySettingRequest request = new StoreDeliverySettingRequest(
                true,
                DeliveryType.SAME_DAY,
                new BigDecimal("-10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                DeliveryGeography.ONLY_IN_THE_CITY
        );
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/delivery-settings", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storeDeliverySettingService, never()).createStoreDeliverySetting(anyLong(), any());
    }

    @Test
    void getStoreDeliverySetting_success() throws Exception {
        StoreDeliverySettingResponse response = new StoreDeliverySettingResponse(
                1L,
                1L,
                true,
                DeliveryType.SAME_DAY,
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                DeliveryGeography.ONLY_IN_THE_CITY
        );
        when(storeDeliverySettingService.getStoreDeliverySetting(anyLong())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-settings", 1L))
                .andExpect(status().isOk());

        verify(storeDeliverySettingService).getStoreDeliverySetting(1L);
    }

    @Test
    void getStoreDeliverySetting_shouldReturnNotFound_whenSettingNotFound() throws Exception {
        when(storeDeliverySettingService.getStoreDeliverySetting(anyLong())).thenThrow(new StoreDeliverySettingNotFoundException("Setting not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/delivery-settings", 1L))
                .andExpect(status().isNotFound());

        verify(storeDeliverySettingService).getStoreDeliverySetting(anyLong());
    }

    @Test
    void updateStoreDeliverySetting_success() throws Exception {
        StoreDeliverySettingResponse response = new StoreDeliverySettingResponse(
                2L,
                2L,
                true,
                DeliveryType.SAME_DAY,
                new BigDecimal("20.00"),
                new BigDecimal("20.00"),
                new BigDecimal("20.00"),
                DeliveryGeography.ONLY_IN_THE_CITY
        );

        when(storeDeliverySettingService.updateStoreDeliverySetting(anyLong(), any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/delivery-settings", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.deliveryType").value("SAME_DAY"))
                .andExpect(jsonPath("$.minOrderPrice").value(20.00))
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(20.00))
                .andExpect(jsonPath("$.maxDistance").value(20.00))
                .andExpect(jsonPath("$.deliveryGeography").value("ONLY_IN_THE_CITY"));

        verify(storeDeliverySettingService).updateStoreDeliverySetting(anyLong(), any());
    }

    @Test
    void updateStoreDeliverySetting_shouldReturnNotFound_whenSettingNotFound() throws Exception {
        when(storeDeliverySettingService.updateStoreDeliverySetting(anyLong(), any())).thenThrow(new StoreDeliverySettingNotFoundException("Setting not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/delivery-settings", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isNotFound());

        verify(storeDeliverySettingService).updateStoreDeliverySetting(anyLong(), any());
    }
}
