package com.delivery.habsida.controller;

import com.delivery.habsida.dto.OrderCreateRequest;
import com.delivery.habsida.dto.OrderItemRequest;
import com.delivery.habsida.dto.OrderRejectRequest;
import com.delivery.habsida.dto.OrderResponse;
import com.delivery.habsida.entity.OrderType;
import com.delivery.habsida.entity.PaymentMethod;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.OrderService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    private OrderResponse defaultResponse() {
        return new OrderResponse(
                1L,
                2L,
                3L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private OrderCreateRequest defaultRequest() {
        return new OrderCreateRequest(
                OrderType.PICKUP,
                PaymentMethod.CASH,
                null,
                3L,
                4L,
                List.of(new OrderItemRequest(6L, 2, List.of(7L)))
        );
    }

    private OrderRejectRequest defaultReject() {
        return new OrderRejectRequest(
                "reason"
        );
    }

    @Test
    void createOrder_success() throws Exception {
        when(orderService.createOrder(eq(2L), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.customerId").value(3L));
    }

    @Test
    void acceptOrder_success() throws Exception {
        when(orderService.acceptOrder(2L, 1L)).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders/{orderId}/accept", 2L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.customerId").value(3L));
    }

    @Test
    void acceptOrder_whenAccessDenied() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders/{orderId}/accept", 2L, 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectOrder_success() throws Exception {
        when(orderService.rejectOrder(2L, 1L, "reason")).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders/{orderId}/reject", 2L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultReject())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.customerId").value(3L));
    }

    @Test
    void cancelOrder_success() throws Exception {
        when(orderService.cancelOrder(2L, 1L)).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders/{orderId}/cancel", 2L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.customerId").value(3L));
    }

    @Test
    void startOrder_success() throws Exception {
        when(orderService.startOrder(2L, 1L)).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders/{orderId}/start", 2L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.customerId").value(3L));
    }

    @Test
    void completeOrder_success() throws Exception {
        when(orderService.completeOrder(2L, 1L)).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/orders/{orderId}/complete", 2L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.customerId").value(3L));
    }

    @Test
    void newOrder_success() throws Exception {
        when(orderService.getNewOrders(eq(2L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/orders/new", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].storeId").value(2L))
                .andExpect(jsonPath("$.content[0].customerId").value(3L));
    }
}
