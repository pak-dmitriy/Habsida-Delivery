package com.delivery.habsida.controller;

import com.delivery.habsida.dto.OrderPaymentDto;
import com.delivery.habsida.dto.OrderPaymentRequest;
import com.delivery.habsida.entity.PaymentMethod;
import com.delivery.habsida.entity.PaymentStatus;
import com.delivery.habsida.exception.OrderNotFoundException;
import com.delivery.habsida.exception.PaymentNotFoundException;
import com.delivery.habsida.exception.TransactionNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.OrderPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderPaymentController.class)
@Import(WebSecurityConfig.class)

public class OrderPaymentControllerTest {
    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderPaymentService orderPaymentService;

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
    void getOrderPayment_success() throws Exception {
        OrderPaymentDto response = new OrderPaymentDto(
                1L,
                1L,
                PaymentStatus.PAID,
                PaymentMethod.CASH,
                LocalDateTime.now(),
                "Provider",
                null
        );
        when(orderPaymentService.getOrderPayment(anyLong(), anyLong())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.method").value("CASH"))
                .andExpect(jsonPath("$.provider").value("Provider"))
                .andExpect(jsonPath("$.transaction").isEmpty());
    }

    @WithMockUser
    @Test
    void getOrderPayment_shouldReturn404_whenPaymentNotFound() throws Exception {
        when(orderPaymentService.getOrderPayment(anyLong(), anyLong())).thenThrow(new PaymentNotFoundException("Payment not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void getOrderPayment_shouldReturn404_whenOrderNotFound() throws Exception {
        when(orderPaymentService.getOrderPayment(anyLong(), anyLong())).thenThrow(new OrderNotFoundException("Order not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void updateOrderPayment_success() throws Exception {
        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        OrderPaymentDto response = new OrderPaymentDto(
                1L,
                1L,
                PaymentStatus.PAID,
                PaymentMethod.CASH,
                LocalDateTime.now(),
                "Provider",
                null
        );

        when(orderPaymentService.updateOrderPayment(anyLong(), anyLong(), any())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.method").value("CASH"))
                .andExpect(jsonPath("$.provider").value("Provider"))
                .andExpect(jsonPath("$.transaction").isEmpty());
    }

    @WithMockUser
    @Test
    void updateOrderPayment_shouldReturn404_whenStatusIsNull() throws Exception {
        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                null,
                null
        );
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void updateOrderPayment_shouldReturn404_whenTransactionNotFound() throws Exception {
        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        when(orderPaymentService.updateOrderPayment(anyLong(), anyLong(), any())).thenThrow(new TransactionNotFoundException("Transaction not found"));
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void updateOrderPayment_shouldReturn404_whenOrderNotFound() throws Exception {
        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        when(orderPaymentService.updateOrderPayment(anyLong(), anyLong(), any())).thenThrow(new OrderNotFoundException("Order not found"));
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void updateOrderPayment_shouldReturn404_whenPaymentNotFound() throws Exception {
        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        when(orderPaymentService.updateOrderPayment(anyLong(), anyLong(), any())).thenThrow(new PaymentNotFoundException("Payment not found"));
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/orders/{orderId}/payment", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
