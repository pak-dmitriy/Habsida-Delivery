package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ProductCreateRequest;
import com.delivery.habsida.dto.ProductDto;
import com.delivery.habsida.entity.ProductStatus;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(WebSecurityConfig.class)

class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @WithMockUser
    @Test
    void createProduct_shouldReturn201_whenRequestIsValid() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

        ProductCreateRequest request = new ProductCreateRequest(
                "name",
                "description",
                BigDecimal.valueOf(100),
                10,
                5,
                ProductStatus.AVAILABLE,
                20,
                50,
                1,
                1L
        );

        ProductDto dto = new ProductDto(
                1L,
                "name",
                "description",
                BigDecimal.valueOf(100),
                50,
                10,
                ProductStatus.AVAILABLE,
                200,
                20,
                1,
                1L,
                1L
        );

        when(productService.createProduct(anyLong(), any())).thenReturn(dto);
        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/products", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @WithMockUser
    @Test
    void createProduct_shouldReturn400_whenNameIsBlank() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

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

    }

    @WithMockUser
    @Test
    void getProducts_shouldReturnListOfProducts() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        ProductDto dto = new ProductDto(
                1L,
                "name",
                "description",
                BigDecimal.valueOf(100),
                50,
                10,
                ProductStatus.AVAILABLE,
                200,
                20,
                1,
                1L,
                1L
        );
        when(productService.getProducts(anyLong(), any(), any())).thenReturn(List.of(dto));
        mockMvc.perform(get("/stores/{storeId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

        ProductDto dto = new ProductDto(
                1L,
                "name",
                "description",
                BigDecimal.valueOf(100),
                50,
                10,
                ProductStatus.AVAILABLE,
                200,
                20,
                1,
                1L,
                1L
        );

        when(productService.getProduct(anyLong(), anyLong())).thenReturn(dto);
        mockMvc.perform(get("/stores/{storeId}/products/{productId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void updateProduct_shouldReturn200_whenRequestIsValid() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

        ProductCreateRequest request = new ProductCreateRequest(
                "name",
                "description",
                BigDecimal.valueOf(100),
                10,
                5,
                ProductStatus.AVAILABLE,
                20,
                50,
                1,
                1L
        );

        ProductDto dto = new ProductDto(
                1L,
                "name",
                "description",
                BigDecimal.valueOf(100),
                50,
                10,
                ProductStatus.AVAILABLE,
                20,
                20,
                1,
                1L,
                1L
        );

        when(productService.updateProduct(anyLong(), anyLong(), any())).thenReturn(dto);
        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/products/{productId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void deleteProduct_shouldReturn204_whenRequestIsValid() throws Exception {

        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

        mockMvc.perform(delete("/stores/{storeId}/products/{productId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
