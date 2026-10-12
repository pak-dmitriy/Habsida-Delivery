package com.delivery.habsida.service;

import com.delivery.habsida.dto.CategoryCreateRequest;
import com.delivery.habsida.dto.CategoryResponse;
import com.delivery.habsida.entity.Category;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.CategoryNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.CategoryRepository;
import com.delivery.habsida.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Store store;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        category = new Category();
        category.setName("test");
        category.setDescription("description");
        category.setCategoryPhoto("categoryPhoto");
        category.setStore(store);
        category.setId(5L);
    }

    private CategoryCreateRequest defaultRequest() {
        return new CategoryCreateRequest(
                "name",
                "desc",
                "photo"
        );
    }

    @Test
    void createCategory_shouldCreateCategory() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse result = categoryService.createCategory(1L, defaultRequest());

        assertEquals("name", result.name());
        assertEquals("desc", result.description());
        assertEquals("photo", result.categoryPhoto());
        assertEquals(1L, result.storeId());

        verify(categoryRepository).save(any());
    }

    @Test
    void createCategory_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> categoryService.createCategory(1L, defaultRequest()));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategories_shouldReturnPageOfCategories() {
        when(categoryRepository.findByStoreId(1L, pageable)).thenReturn(new PageImpl<>(List.of(category)));

        Page<CategoryResponse> page = categoryService.getCategories(1L, pageable, null);
        CategoryResponse response = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals("test", response.name());
        assertEquals("description", response.description());
        assertEquals("categoryPhoto", response.categoryPhoto());
        assertEquals(5L, response.id());
        assertEquals(1L, response.storeId());

        verify(categoryRepository).findByStoreId(1L, pageable);
    }

    @Test
    void getCategories_shouldReturnEmptyPage_whenNoCategoriesExist() {
        when(categoryRepository.findByStoreId(1L, pageable)).thenReturn(Page.empty());

        Page<CategoryResponse> page = categoryService.getCategories(1L, pageable, null);

        assertNotNull(page);
        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());

        verify(categoryRepository).findByStoreId(1L, pageable);
    }

    @Test
    void updateCategory_shouldUpdateCategory() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse updatedCategory = categoryService.updateCategory(1L, 5L, defaultRequest());

        assertEquals("name", updatedCategory.name());

        verify(categoryRepository).findById(5L);
    }

    @Test
    void updateCategory_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(1L, 5L, defaultRequest()));

        verify(categoryRepository).findById(5L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_shouldThrowException_whenCategoryBelongsToDifferentStore() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(99L, 5L, defaultRequest()));

        verify(categoryRepository).findById(5L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_shouldDeleteCategory() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L, 5L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(1L, 5L));

        verify(categoryRepository).findById(5L);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_shouldThrowException_whenCategoryBelongsToDifferentStore() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(99L, 5L));

        verify(categoryRepository).findById(5L);
        verify(categoryRepository, never()).delete(any());
    }
}

