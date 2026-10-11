package com.delivery.habsida.controller;

import com.delivery.habsida.dto.CustomerCreateRequest;
import com.delivery.habsida.dto.CustomerResponse;
import com.delivery.habsida.dto.CustomerUpdateRequest;
import com.delivery.habsida.entity.CustomerStatus;
import com.delivery.habsida.exception.CustomerAlreadyExistsException;
import com.delivery.habsida.exception.CustomerNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.CustomerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(WebSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    private CustomerResponse defaultResponse() {
        return new CustomerResponse(
                1L,
                "name",
                "123456",
                CustomerStatus.ACTIVE
        );
    }

    private CustomerCreateRequest defaultRequest() {
        return new CustomerCreateRequest(
                "name",
                "123456"
        );
    }

    @Test
    void createCustomer_success() throws Exception {
        when(customerService.createCustomer(any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.phone").value("123456"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(customerService).createCustomer(any());
    }

    @Test
    void createCustomer_shouldReturn409_whenPhoneIsAlreadyUsed() throws Exception {
        when(customerService.createCustomer(any())).thenThrow(new CustomerAlreadyExistsException("Customer with this phone already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isConflict());

        verify(customerService).createCustomer(any());
    }

    @Test
    void getAllCustomers_success() throws Exception {
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("name"))
                .andExpect(jsonPath("$.content[0].phone").value("123456"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        verify(customerService).getAllCustomers(any(Pageable.class));
    }

    @Test
    void getCustomerById_success() throws Exception {
        when(customerService.getCustomerById(anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/customers/{customerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.phone").value("123456"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(customerService).getCustomerById(anyLong());
    }

    @Test
    void getCustomerById_shouldReturn404_whenCustomerNotFound() throws Exception {
        when(customerService.getCustomerById(anyLong())).thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/customers/{customerId}", 1L))
                .andExpect(status().isNotFound());

        verify(customerService).getCustomerById(anyLong());
    }

    @Test
    void updateCustomer_success() throws Exception {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "name1",
                "654321",
                CustomerStatus.BLOCKED
        );

        CustomerResponse updateResponse = new CustomerResponse(
                22L,
                "name1",
                "654321",
                CustomerStatus.BLOCKED
        );

        when(customerService.updateCustomer(eq(22L), any(CustomerUpdateRequest.class))).thenReturn(updateResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/customers/{customerId}", 22L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22L))
                .andExpect(jsonPath("$.name").value("name1"))
                .andExpect(jsonPath("$.phone").value("654321"))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(customerService).updateCustomer(eq(22L), any(CustomerUpdateRequest.class));
    }

    @Test
    void updateCustomer_shouldReturn400() throws Exception {
        CustomerUpdateRequest invalidRequest = new CustomerUpdateRequest(
                " ",
                "654321",
                CustomerStatus.BLOCKED
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/customers/{customerId}", 22L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).updateCustomer(anyLong(), any());
    }

    @Test
    void deleteCustomer_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/customers/{customerId}", 22L))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(anyLong());
    }

    @Test
    void deleteCustomer_shouldReturn404_whenCustomerNotFound() throws Exception {
        doThrow(new CustomerNotFoundException ("Customer not found"))
                .when(customerService).deleteCustomer(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/customers/{customerId}", 22L))
                .andExpect(status().isNotFound());

        verify(customerService).deleteCustomer(22L);
    }
}
