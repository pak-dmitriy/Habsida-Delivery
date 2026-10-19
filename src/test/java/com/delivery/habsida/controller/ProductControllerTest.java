package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ProductCreateRequest;
import com.delivery.habsida.dto.ProductResponse;
import com.delivery.habsida.entity.ProductStatus;
import com.delivery.habsida.exception.InvalidQuantityException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ProductService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(ProductController.class)
@Import(WebSecurityConfig.class)
class ProductControllerTest {
    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    private ProductCreateRequest defaultRequest() {
        return new ProductCreateRequest(
                "name",
                "description",
                BigDecimal.valueOf(100),
                50,
                5,
                ProductStatus.AVAILABLE,
                20,
                50,
                1,
                1L
        );
    }

    private ProductResponse defaultResponse() {
        return new ProductResponse(
                1L,
                "name",
                "description",
                BigDecimal.valueOf(100),
                50,
                5,
                ProductStatus.AVAILABLE,
                20,
                50,
                1,
                1L,
                1L
        );
    }

    @Test
    void createProduct_shouldReturn201_whenRequestIsValid() throws Exception {
        when(productService.createProduct(anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/products", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.lowStockThreshold").value(5))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.discountPercent").value(20))
                .andExpect(jsonPath("$.maxQuantity").value(50))
                .andExpect(jsonPath("$.minQuantity").value(1))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.categoryId").value(1L));

        verify(productService).createProduct(anyLong(), any());
    }

    @Test
    void createProduct_shouldReturn400_whenNameIsBlank() throws Exception {
        ProductCreateRequest invalidRequest = new ProductCreateRequest(
                " ",
                "description",
                BigDecimal.valueOf(100),
                10,
                5,
                ProductStatus.AVAILABLE,
                200,
                50,
                1,
                1L
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/products", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(anyLong(), any());
    }

    @Test
    void getProducts_shouldReturnListOfProducts() throws Exception {
        when(productService.getProducts(anyLong(),
                any(),
                any(),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(get("/stores/{storeId}/products", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("name"))
                .andExpect(jsonPath("$.content[0].description").value("description"))
                .andExpect(jsonPath("$.content[0].price").value(100))
                .andExpect(jsonPath("$.content[0].stock").value(50))
                .andExpect(jsonPath("$.content[0].lowStockThreshold").value(5))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[0].discountPercent").value(20))
                .andExpect(jsonPath("$.content[0].maxQuantity").value(50))
                .andExpect(jsonPath("$.content[0].minQuantity").value(1))
                .andExpect(jsonPath("$.content[0].storeId").value(1L))
                .andExpect(jsonPath("$.content[0].categoryId").value(1L));

        verify(productService).getProducts(anyLong(), any(), any(), any(Pageable.class));
    }

    @Test
    void getProducts_shouldReturn400_whenStatusIsInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/products", 1L, 1L).param("status", "0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProducts(
                anyLong(), anyLong(), any(), any(Pageable.class));
    }

    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        when(productService.getProduct(anyLong(), anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(get("/stores/{storeId}/products/{productId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.lowStockThreshold").value(5))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.discountPercent").value(20))
                .andExpect(jsonPath("$.maxQuantity").value(50))
                .andExpect(jsonPath("$.minQuantity").value(1))
                .andExpect(jsonPath("$.storeId").value(1L))
                .andExpect(jsonPath("$.categoryId").value(1L));

        verify(productService).getProduct(anyLong(), anyLong());
    }

    @Test
    void getProduct_shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.getProduct(
                anyLong(),
                anyLong()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/products/{productId}", 1L, 1L))
                .andExpect(status().isNotFound());

        verify(productService).getProduct(anyLong(), anyLong());
    }

    @Test
    void updateProduct_shouldReturn200_whenRequestIsValid() throws Exception {
        ProductResponse updateResponse = new ProductResponse(
                2L,
                "name1",
                "description1",
                BigDecimal.valueOf(100),
                60,
                11,
                ProductStatus.PAUSED,
                30,
                30,
                2,
                2L,
                2L
        );

        when(productService.updateProduct(anyLong(), anyLong(), any())).thenReturn(updateResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/products/{productId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("name1"))
                .andExpect(jsonPath("$.description").value("description1"))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.stock").value(60))
                .andExpect(jsonPath("$.lowStockThreshold").value(11))
                .andExpect(jsonPath("$.status").value("PAUSED"))
                .andExpect(jsonPath("$.discountPercent").value(30))
                .andExpect(jsonPath("$.maxQuantity").value(30))
                .andExpect(jsonPath("$.minQuantity").value(2))
                .andExpect(jsonPath("$.storeId").value(2L))
                .andExpect(jsonPath("$.categoryId").value(2L));
;
        verify(productService).updateProduct(anyLong(), anyLong(), any());
    }

    @Test
    void updateProduct_shouldReturn400_whenMinQuantityGreaterThanMaxQuantity() throws Exception {
        when(productService.updateProduct(
                anyLong(),
                anyLong(),
                any()))
                .thenThrow(new InvalidQuantityException ("MinQuantity cannot be greater than MaxQuantity"));

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/products/{productId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isBadRequest());

        verify(productService).updateProduct(anyLong(), anyLong(), any());
    }

    @Test
    void deleteProduct_shouldReturn204_whenRequestIsValid() throws Exception {
        mockMvc.perform(delete("/stores/{storeId}/products/{productId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(anyLong(), anyLong());
    }

    @Test
    void deleteProduct_shouldReturn404_whenProductNotFound() throws Exception {
        doThrow(new ProductNotFoundException("Product not found"))
                .when(productService).deleteProduct(anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/products/{productId}", 1L, 1L))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(anyLong(), anyLong());
    }
}
