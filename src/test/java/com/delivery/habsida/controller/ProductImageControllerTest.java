package com.delivery.habsida.controller;

import com.delivery.habsida.dto.ProductImageRequest;
import com.delivery.habsida.dto.ProductImageResponse;
import com.delivery.habsida.exception.ImageNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.ProductImageService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductImageController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
class ProductImageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductImageService productImageService;

    @MockitoBean
    private JwtService jwtService;

    private ProductImageRequest defaultRequest() {
        return new ProductImageRequest(
                "image",
                0
        );
    }

    private ProductImageResponse defaultResponse() {
        return new ProductImageResponse(
                1L,
                "image",
                0
        );
    }

    @Test
    void createProductImage_shouldReturn201_whenRequestIsValid() throws Exception {
        when(productImageService.createImages(anyLong(), anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(post("/stores/{storeId}/products/{productId}/images", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imagePhoto").value("image"))
                .andExpect(jsonPath("$.sortOrder").value(0));

        verify(productImageService).createImages(eq(1L), eq(5L), any());
    }

    @Test
    void createProductImage_shouldReturn400_whenImagePhotoIsBlank() throws Exception {
        ProductImageRequest invalidRequest = new ProductImageRequest(
                " ",
                5);

        mockMvc.perform(post("/stores/{storeId}/products/{productId}/images", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productImageService, never()).createImages(anyLong(), anyLong(), any());
    }

    @Test
    void createProductImage_shouldReturn400_whenSortOrderIsNegative() throws Exception {
        ProductImageRequest invalidRequest = new ProductImageRequest(
                "imagePhoto",
                -5
        );

        mockMvc.perform(post("/stores/{storeId}/products/{productId}/images", 1L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productImageService, never()).createImages(anyLong(), anyLong(), any());
    }

    @Test
    void getProductImages_shouldReturnPageOfProductImages() throws Exception {
        when(productImageService.getProductImages(
                anyLong(),
                anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(get("/stores/{storeId}/products/{productId}/images", 1L, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].imagePhoto").value("image"))
                .andExpect(jsonPath("$.content[0].sortOrder").value(0));

        verify(productImageService).getProductImages(eq(1L), eq(5L), any(Pageable.class));
    }

    @Test
    void deleteProductImages_shouldReturn204_whenRequestIsValid() throws Exception {
        mockMvc.perform(delete("/stores/{storeId}/products/{productId}/images/{imageId}",
                        1L, 5L, 1L))
                .andExpect(status().isNoContent());

        verify(productImageService).deleteProductImages(anyLong(), anyLong(), any());
    }

    @Test
    void deleteProductImages_shouldReturn404_whenImageNotFound() throws Exception {
        doThrow(new ImageNotFoundException("Image not found"))
                .when(productImageService).deleteProductImages(anyLong(), anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/products/{productId}/images/{imageId}",
                        1L, 5L, 1L))
                .andExpect(status().isNotFound());

        verify(productImageService).deleteProductImages(anyLong(), anyLong(), anyLong());
    }
}
