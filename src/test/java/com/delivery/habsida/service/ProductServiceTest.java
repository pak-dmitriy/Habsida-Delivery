package com.delivery.habsida.service;

import com.delivery.habsida.dto.ProductCreateRequest;
import com.delivery.habsida.dto.ProductResponse;
import com.delivery.habsida.entity.Category;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductStatus;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.CategoryNotFoundException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.exception.StoreNotFoundException;
import com.delivery.habsida.repository.CategoryRepository;
import com.delivery.habsida.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Store store;
    private Category category;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        category = new Category();
        category.setId(1L);
        category.setStore(store);
    }

    private Product defaultProduct() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("test");
        product1.setDescription("description");
        product1.setPrice(new BigDecimal("10.2"));
        product1.setStock(50);
        product1.setLowStockThreshold(10);
        product1.setStatus(ProductStatus.AVAILABLE);
        product1.setDiscountPercent(5);
        product1.setMaxQuantity(100);
        product1.setMinQuantity(5);
        product1.setStore(store);
        product1.setCategory(category);

        return product1;
    }

    private ProductCreateRequest defaultRequest() {
        return new ProductCreateRequest(
                "test",
                "description",
                new BigDecimal("10.2"),
                50,
                10,
                ProductStatus.AVAILABLE,
                5,
                100,
                5,
                1L
        );
    }

    @Test
    void createProduct_shouldCreateProduct() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(defaultProduct());

        ProductResponse result = productService.createProduct(1L, defaultRequest());

        assertEquals("test", result.name());
        assertEquals("description", result.description());
        assertEquals(new BigDecimal("10.2"), result.price());
        assertEquals(50, result.stock());
        assertEquals(10, result.lowStockThreshold());
        assertEquals(ProductStatus.AVAILABLE, result.status());
        assertEquals(5, result.discountPercent());
        assertEquals(100, result.maxQuantity());
        assertEquals(5, result.minQuantity());
        assertEquals(1L, result.categoryId());

        verify(productRepository).save(any());
    }

    @Test
    void createProduct_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> productService.createProduct(1L, defaultRequest()));

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_shouldThrowException_whenCategoryNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> productService.createProduct(1L, defaultRequest()));

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_shouldThrowException_whenCategoryBelongsToDifferentStore() {
        when(storeRepository.findById(99L)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotFoundException.class,
                () -> productService.createProduct(99L, defaultRequest()));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProducts_withBothFilters() {
        when(productRepository.findByStoreIdAndCategoryIdAndStatus(
                1L,
                1L,
                ProductStatus.AVAILABLE, pageable))
                .thenReturn(new PageImpl<>(List.of(defaultProduct()), pageable, 1L));

        Page<ProductResponse> page = productService.getProducts(1L, 1L, ProductStatus.AVAILABLE, pageable);
        ProductResponse dto = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, dto.categoryId());
        assertEquals("test", dto.name());
        assertEquals("description", dto.description());
        assertEquals(new BigDecimal("10.2"), dto.price());
        assertEquals(50, dto.stock());
        assertEquals(10, dto.lowStockThreshold());
        assertEquals(ProductStatus.AVAILABLE, dto.status());
        assertEquals(100, dto.maxQuantity());
        assertEquals(5, dto.minQuantity());
    }

    @Test
    void getProducts_withCategoryFilter() {
        when(productRepository.findByStoreIdAndCategoryId(
                1L,
                1L,
                pageable)).thenReturn(new PageImpl<>(List.of(defaultProduct()), pageable, 1L));

        Page<ProductResponse> page = productService.getProducts(1L, 1L, null, pageable);
        ProductResponse dto = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, dto.categoryId());
        assertEquals("test", dto.name());
        assertEquals("description", dto.description());
        assertEquals(new BigDecimal("10.2"), dto.price());
        assertEquals(50, dto.stock());
        assertEquals(10, dto.lowStockThreshold());
        assertEquals(ProductStatus.AVAILABLE, dto.status());
        assertEquals(100, dto.maxQuantity());
        assertEquals(5, dto.minQuantity());
    }

    @Test
    void getProducts_withStatusFilters() {
        when(productRepository.findByStoreIdAndStatus(1L,
                ProductStatus.AVAILABLE, pageable))
                .thenReturn(new PageImpl<>(List.of(defaultProduct())));

        Page<ProductResponse> page = productService.getProducts(1L,
                null, ProductStatus.AVAILABLE, pageable);

        ProductResponse dto = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, dto.categoryId());
        assertEquals("test", dto.name());
        assertEquals("description", dto.description());
        assertEquals(new BigDecimal("10.2"), dto.price());
        assertEquals(50, dto.stock());
        assertEquals(10, dto.lowStockThreshold());
        assertEquals(ProductStatus.AVAILABLE, dto.status());
        assertEquals(100, dto.maxQuantity());
        assertEquals(5, dto.minQuantity());
    }

    @Test
    void getProducts_withoutFilters() {
        when(productRepository.findByStoreId(1L, pageable)).thenReturn(new PageImpl<>(List.of(defaultProduct())));

        Page<ProductResponse> page = productService.getProducts(
                1L,
                null,
                null,
                pageable);

        ProductResponse dto = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(1L, dto.categoryId());
        assertEquals("test", dto.name());
        assertEquals("description", dto.description());
        assertEquals(new BigDecimal("10.2"), dto.price());
        assertEquals(50, dto.stock());
        assertEquals(10, dto.lowStockThreshold());
        assertEquals(ProductStatus.AVAILABLE, dto.status());
        assertEquals(100, dto.maxQuantity());
        assertEquals(5, dto.minQuantity());
    }

    @Test
    void getProduct_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));

        ProductResponse productResponse = productService.getProduct(1L, 1L);

        assertEquals(1L, productResponse.id());
        assertEquals("test", productResponse.name());
    }

    @Test
    void getProduct_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProduct(1L, 1L));
    }

    @Test
    void getProduct_shouldThrowException_whenProductBelongsToDifferentStore() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(defaultProduct()));

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProduct(99L, 1L));
    }

    @Test
    void updateProduct_shouldUpdateProduct() {
        ProductCreateRequest updatedProduct = new ProductCreateRequest(
                "test1",
                "description1",
                new BigDecimal("200.22"),
                60,
                20,
                ProductStatus.PAUSED,
                6,
                200,
                2,
                2L
        );
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = productService.updateProduct(1L, 1L, updatedProduct);

        assertEquals("test1", result.name());
        assertEquals("description1", result.description());
        assertEquals(new BigDecimal("200.22"), result.price());
        assertEquals(60, result.stock());
        assertEquals(20, result.lowStockThreshold());
        assertEquals(ProductStatus.PAUSED, result.status());
        assertEquals(6, result.discountPercent());
        assertEquals(200, result.maxQuantity());
        assertEquals(2, result.minQuantity());
        assertEquals(1L, result.categoryId());
    }

    @Test
    void updateProduct_shouldThrowException_whenProductNotFound() {
        ProductCreateRequest updatedProduct = new ProductCreateRequest(
                "test",
                "description",
                new BigDecimal("100.22"),
                50,
                10,
                ProductStatus.AVAILABLE,
                5,
                100,
                1,
                1L
        );
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(1L, 1L, updatedProduct));

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_shouldThrowException_whenProductBelongsToDifferentStore() {
        ProductCreateRequest updatedProduct = new ProductCreateRequest(
                "test",
                "description",
                new BigDecimal("100.22"),
                50,
                10,
                ProductStatus.AVAILABLE,
                5,
                100,
                1,
                1L
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));

        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(99L, 1L, updatedProduct));

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryNotFound() {
        ProductCreateRequest updatedProduct = new ProductCreateRequest(
                "test",
                "description",
                new BigDecimal("100.22"),
                50,
                10,
                ProductStatus.AVAILABLE,
                5,
                100,
                1,
                1L
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> productService.updateProduct(1L, 1L, updatedProduct));

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryBelongsToDifferentStore() {
        ProductCreateRequest updatedProduct = new ProductCreateRequest(
                "test",
                "description",
                new BigDecimal("100.22"),
                50,
                10,
                ProductStatus.AVAILABLE,
                5,
                100,
                1,
                1L
        );

        Store otherStore = new Store();
        otherStore.setId(2L);
        category.setStore(otherStore); // переопределяем магазин категории — только для этого теста

        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotFoundException.class,
                () -> productService.updateProduct(1L, 1L, updatedProduct));

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_shouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));

        productService.deleteProduct(1L, 1L);

        verify(productRepository).delete(any());
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(1L, 1L));

        verify(productRepository, never()).delete(any());
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductBelongsToDifferentStore() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(defaultProduct()));

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(99L, 1L));

        verify(productRepository, never()).delete(any());
    }
}
