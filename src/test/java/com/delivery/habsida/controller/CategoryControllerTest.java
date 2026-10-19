package com.delivery.habsida.controller;

import com.delivery.habsida.dto.CategoryCreateRequest;
import com.delivery.habsida.dto.CategoryResponse;
import com.delivery.habsida.exception.CategoryNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.StoreAccessGuard;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean(name = "storeAccessGuard")
    private StoreAccessGuard storeAccessGuard;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(storeAccessGuard.canAccessStore(any(), anyLong())).thenReturn(true);
    }

    private CategoryResponse defaultResponse() {
        return new CategoryResponse(
                1L,
                "test",
                "desc",
                "photo",
                1L
        );
    }

    private CategoryCreateRequest defaultRequest() {
        return new CategoryCreateRequest(
                "test",
                "desc",
                "photo");
    }

    @Test
    void createCategory_shouldReturn201_whenRequestIsValid() throws Exception {
        when(categoryService.createCategory(anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/categories", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated());

        verify(categoryService).createCategory(anyLong(),any());
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        CategoryCreateRequest invalidRequest = new CategoryCreateRequest(
                "",
                "desc",
                "photo");

        mockMvc.perform(post("/stores/{storeId}/categories", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(anyLong(), any());
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories() throws Exception {
        when(categoryService.getCategories(
                anyLong(),
                any(Pageable.class),
                any())).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(get("/stores/{storeId}/categories", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("test"))
                .andExpect(jsonPath("$.content[0].description").value("desc"))
                .andExpect(jsonPath("$.content[0].categoryPhoto").value("photo"))
                .andExpect(jsonPath("$.content[0].storeId").value(1L));

        verify(categoryService).getCategories(anyLong(), any(Pageable.class), any());
    }

    @Test
    void getAllCategories_shouldReturnNotFound_whenStoreNotFound() throws Exception {
        when(categoryService.getCategories(anyLong(), any(Pageable.class), any())).thenThrow(new StoreNotFoundException("Store not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/categories", 1L))
                .andExpect(status().isNotFound());

        verify(categoryService).getCategories(anyLong(), any(Pageable.class), any());
    }

    @Test
    void updateCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        CategoryCreateRequest invalidRequest = new CategoryCreateRequest(
                "",
                "desc",
                "photo");

        mockMvc.perform(put("/stores/{storeId}/categories/{categoryId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(anyLong(), anyLong(), any());
    }

    @Test
    void updateCategory_shouldReturn200_whenRequestIsValid() throws Exception {
        CategoryResponse updateResponse = new CategoryResponse(
                2L,
                "test1",
                "desc1",
                "photo1",
                2L
        );

        when(categoryService.updateCategory(anyLong(), anyLong(), any())).thenReturn(updateResponse);

        mockMvc.perform(put("/stores/{storeId}/categories/{categoryId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk());

        verify(categoryService).updateCategory(anyLong(), anyLong(), any());
    }

    @Test
    void deleteCategory_shouldReturn204_whenRequestIsValid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/categories/{categoryId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(anyLong(), anyLong());
    }

    @Test
    void deleteCategory_shouldReturn404_whenCategoryNotFound() throws Exception {
        doThrow(new CategoryNotFoundException("Category not found"))
                .when(categoryService).deleteCategory(anyLong(),  anyLong());

        mockMvc.perform(delete("/stores/{storeId}/categories/{categoryId}", 1L, 1L))
                .andExpect(status().isNotFound());

        verify(categoryService).deleteCategory(anyLong(), anyLong());
    }
}
