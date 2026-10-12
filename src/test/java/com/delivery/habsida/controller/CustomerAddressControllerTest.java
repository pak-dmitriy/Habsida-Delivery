package com.delivery.habsida.controller;

import com.delivery.habsida.dto.CustomerAddressCreateRequest;
import com.delivery.habsida.dto.CustomerAddressResponse;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.CustomerAddressService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerAddressController.class)
@Import(WebSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
public class CustomerAddressControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerAddressService customerAddressService;

    @MockitoBean
    private JwtService jwtService;

    private CustomerAddressCreateRequest defaultRequest() {
        return new CustomerAddressCreateRequest(
                "city",
                "street",
                "house",
                "apartment"
        );
    }

    private CustomerAddressResponse defaultResponse() {
        return new CustomerAddressResponse(
                5L,
                "city",
                "street",
                "house",
                "apartment",
                1L
        );
    }

    @Test
    void createCustomerAddress_success() throws Exception {
        when(customerAddressService.createAddress(anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/customers/{customerId}/addresses", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.city").value("city"))
                .andExpect(jsonPath("$.street").value("street"))
                .andExpect(jsonPath("$.house").value("house"))
                .andExpect(jsonPath("$.apartment").value("apartment"))
                .andExpect(jsonPath("$.customerId").value(1L));

        verify(customerAddressService).createAddress(eq(1L), any());
    }

    @Test
    void getCustomerAddresses_success() throws Exception {
        when(customerAddressService.getAddressesByCustomerId(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/customers/{customerId}/addresses", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[0].city").value("city"))
                .andExpect(jsonPath("$.content[0].street").value("street"))
                .andExpect(jsonPath("$.content[0].house").value("house"))
                .andExpect(jsonPath("$.content[0].apartment").value("apartment"))
                .andExpect(jsonPath("$.content[0].customerId").value(1L));

        verify(customerAddressService).getAddressesByCustomerId(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void getCustomerAddresses_shouldReturn403_whenUserIsMerchant() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/customers/{customerId}/addresses", 1L))
                .andExpect(status().isForbidden());

        verify(customerAddressService, never()).getAddressesByCustomerId(anyLong(), any(Pageable.class));
    }

    @Test
    void updateAddress_success() throws Exception {
        when(customerAddressService.updateAddress(anyLong(), anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/customers/{customerId}/addresses/{addressId}", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.city").value("city"));

        verify(customerAddressService).updateAddress(eq(1L), eq(5L), any());
    }

    @Test
    void deleteAddress_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/customers/{customerId}/addresses/{addressId}", 1L, 5L))
                .andExpect(status().isNoContent());

        verify(customerAddressService).deleteAddress(1L, 5L);
    }
}
