package com.delivery.habsida.controller;

import com.delivery.habsida.dto.CategoryCreateRequest;
import com.delivery.habsida.dto.CategoryDTO;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.CategoryService;
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
@WebMvcTest(CategoryController.class)
@Import(WebSecurityConfig.class)

class CategoryControllerTest  {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @WithMockUser
    @Test
    void getAllCategories_shouldReturnListOfCategories() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        CategoryDTO dto = new CategoryDTO(1L, "test", "desc", "photo", 1L);
        when(categoryService.getCategories(anyLong(), any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/stores/{storeId}/categories", 1L)).andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        CategoryCreateRequest invalidRequest = new CategoryCreateRequest("", "desc", "photo");
        mockMvc.perform(post("/stores/{storeId}/categories", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void createCategory_shouldReturn201_whenRequestIsValid() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        CategoryDTO dto = new CategoryDTO(1L, "test", "desc", "photo", 1L);
        when(categoryService.createCategory(anyLong(), any())).thenReturn(dto);
        CategoryCreateRequest request = new CategoryCreateRequest("test", "desc", "photo");
        mockMvc.perform(post("/stores/{storeId}/categories", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @WithMockUser
    @Test
    void updateCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        CategoryCreateRequest invalidRequest = new CategoryCreateRequest("", "desc", "photo");

        mockMvc.perform(put("/stores/{storeId}/categories/{categoryId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void updateCategory_shouldReturn200_whenRequestIsValid() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
        CategoryDTO dto = new CategoryDTO(1L, "test", "desc", "photo", 1L);
        when(categoryService.updateCategory(anyLong(), anyLong(), any())).thenReturn(dto);
        CategoryCreateRequest request = new CategoryCreateRequest("test", "desc", "photo");

        mockMvc.perform(put("/stores/{storeId}/categories/{categoryId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void deleteCategory_shouldReturn204_whenRequestIsValid() throws Exception {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);

        mockMvc.perform(delete("/stores/{storeId}/categories/{categoryId}", 1L, 1L))
                .andExpect(status().isNoContent());
    }
}
