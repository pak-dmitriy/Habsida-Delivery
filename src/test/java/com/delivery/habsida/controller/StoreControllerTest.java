package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreCreateRequest;
import com.delivery.habsida.dto.StoreResponse;
import com.delivery.habsida.entity.Status;
import com.delivery.habsida.entity.TypeStoreServices;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class StoreControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StoreService storeService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    private StoreResponse defaultResponse() {
        return new StoreResponse(
                1L,
                "name",
                "slug",
                "description",
                TypeStoreServices.RESTAURANT,
                "123456",
                "logo",
                Status.ACTIVE,
                "address",
                new BigDecimal("10.0")
        );
    }

    private StoreCreateRequest defaultRequest() {
        return new StoreCreateRequest(
                "name",
                "slug",
                "description",
                TypeStoreServices.RESTAURANT,
                "123456",
                "logo",
                "address",
                Status.ACTIVE,
                new BigDecimal("10.0")
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createStore_success() throws Exception {
        when(storeService.createStore(any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.storeSlug").value("slug"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.typeStoreServices").value("RESTAURANT"))
                .andExpect(jsonPath("$.phone").value("123456"))
                .andExpect(jsonPath("$.logo").value("logo"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.pickupAddress").value("address"))
                .andExpect(jsonPath("$.deliveryFee").value(10.0));

        verify(storeService).createStore(any());
    }

    @Test
    void createStore_shouldReturn400_whenNameIsBlank() throws Exception {
        StoreCreateRequest blankRequest = new StoreCreateRequest(
                " ",
                "slug",
                "description",
                TypeStoreServices.RESTAURANT,
                "123456",
                "logo",
                "address",
                Status.ACTIVE,
                new BigDecimal("10.0")
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankRequest)))
                .andExpect(status().isBadRequest());

        verify(storeService, never()).createStore(any());
    }

    @Test
    void getStores_success() throws Exception {
        when(storeService.getStores(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("name"))
                .andExpect(jsonPath("$.content[0].storeSlug").value("slug"))
                .andExpect(jsonPath("$.content[0].description").value("description"))
                .andExpect(jsonPath("$.content[0].typeStoreServices").value("RESTAURANT"))
                .andExpect(jsonPath("$.content[0].phone").value("123456"))
                .andExpect(jsonPath("$.content[0].logo").value("logo"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].pickupAddress").value("address"))
                .andExpect(jsonPath("$.content[0].deliveryFee").value(10.0));

        verify(storeService).getStores(any(), any(Pageable.class));
    }

    @Test
    void getStore_success() throws Exception {
        when(storeService.getStore(anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stores/{storeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.storeSlug").value("slug"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.typeStoreServices").value("RESTAURANT"))
                .andExpect(jsonPath("$.phone").value("123456"))
                .andExpect(jsonPath("$.logo").value("logo"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.pickupAddress").value("address"))
                .andExpect(jsonPath("$.deliveryFee").value(10.0));

        verify(storeService).getStore(1L);
    }

    @Test
    void getStore_shouldReturn404_whenStoreNotFound() throws Exception {
        when(storeService.getStore(anyLong())).thenThrow(new StoreNotFoundException("Store not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stores/{storeId}", 1L))
                .andExpect(status().isNotFound());

        verify(storeService).getStore(1L);
    }
}
