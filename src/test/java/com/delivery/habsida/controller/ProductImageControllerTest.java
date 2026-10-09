package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ProductImageDto;
import com.delivery.habsida.dto.ProductImageRequest;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ProductImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductImageController.class)
@Import(WebSecurityConfig.class)

class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductImageService productImageService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @WithMockUser
    @Test
    void createProductImage_shouldReturn201_whenRequestIsValid() throws Exception {

        when(storeAccessGuard.canAccessStore(
                any(),
                anyLong())).thenReturn(true);

        ProductImageDto dto = new ProductImageDto(1L, "imagePhoto", 5);

        when(productImageService.createImages(anyLong(),
                anyLong(),
                any())).thenReturn(dto);

        ProductImageRequest request = new ProductImageRequest(
                "imagePhoto",
                5
        );

        mockMvc.perform(post("/stores/{storeId}/products/{productId}/images", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @WithMockUser
    @Test
    void createProductImage_shouldReturn400_whenImagePhotoIsBlank() throws Exception {
        when(storeAccessGuard.canAccessStore(
                any(),
                anyLong())).thenReturn(true);

        ProductImageRequest invalidRequest = new ProductImageRequest(" ", 5);

        mockMvc.perform(post("/stores/{storeId}/products/{productId}/images", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void createProductImage_shouldReturn400_whenSortOrderIsBlank() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

        ProductImageRequest invalidRequest = new ProductImageRequest(
                "imagePhoto",
                -5
        );

        mockMvc.perform(post("/stores/{storeId}/products/{productId}/images", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void getProductImages_shouldReturnListOfProductImages() throws Exception {

        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        ProductImageDto dto = new ProductImageDto(
                1L,
                "imagePhoto",
                5
        );
        when(productImageService.getProductImages(
                anyLong(),
                anyLong())).thenReturn(List.of(dto));

        mockMvc.perform(get("/stores/{storeId}/products/{productId}/images", 1L, 1L))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void deleteProductImages_shouldReturn204_whenRequestIsValid() throws Exception {
        when(storeAccessGuard.canAccessStore(
                any(),
                anyLong())).thenReturn(true);

        mockMvc.perform(delete("/stores/{storeId}/products/{productId}/images/{imageId}",
                        1L, 1L, 1L))
                .andExpect(status().isNoContent());
    }
}
