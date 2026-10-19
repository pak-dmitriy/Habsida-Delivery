package com.delivery.habsida.service;

import com.delivery.habsida.dto.ProductImageResponse;
import com.delivery.habsida.dto.ProductImageRequest;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductImage;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.exception.ImageNotFoundException;
import com.delivery.habsida.exception.ProductNotFoundException;
import com.delivery.habsida.repository.ProductImageRepository;
import com.delivery.habsida.repository.ProductRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductImageService productImageService;

    private Store store;
    private Product product;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        store = new Store();
        product = new Product();

        store.setId(1L);
        product.setId(5L);
        product.setStore(store);
    }

    private ProductImage defaultImage() {
        ProductImage productImage = new ProductImage();
        productImage.setImagePhoto("image");
        productImage.setSortOrder(0);
        productImage.setId(1L);
        productImage.setProduct(product);

        return productImage;
    }

    private ProductImageRequest defaultRequest() {
        return new ProductImageRequest(
                "image",
                0
        );
    }

    @Test
    void createProductImages_shouldCreateProductImages() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductImageResponse response = productImageService.createImages(1L, 5L, defaultRequest());

        assertEquals("image", response.imagePhoto());
        assertEquals(0, response.sortOrder());

        verify(productImageRepository).save(any());
    }

    @Test
    void createProductImages_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productImageService.createImages(1L, 5L, defaultRequest()));

        verify(productImageRepository, never()).save(any());
    }

    @Test
    void createProductImages_shouldThrowException_whenProductBelongsToDifferentStore() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThrows(ProductNotFoundException.class,
                () -> productImageService.createImages(99L, 5L, defaultRequest()));

        verify(productImageRepository, never()).save(any());
    }

    @Test
    void getProductImages_shouldReturnListOfImages() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.findByProductIdOrderBySortOrderAsc(5L, pageable))
                .thenReturn(new PageImpl<>(List.of(defaultImage())));

        Page<ProductImageResponse> page = productImageService.getProductImages(1L, 5L, pageable);
        ProductImageResponse response = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals("image", response.imagePhoto());
        assertEquals(0, response.sortOrder());
        assertEquals(1L, response.id());

        verify(productImageRepository).findByProductIdOrderBySortOrderAsc(5L, pageable);
    }

    @Test
    void getProductImages_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productImageService.getProductImages(1L, 5L, pageable));

        verify(productImageRepository, never()).findByProductIdOrderBySortOrderAsc(any(), any());
    }

    @Test
    void getProductImages_shouldThrowException_whenProductBelongsToDifferentStore() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThrows(ProductNotFoundException.class,
                () -> productImageService.getProductImages(99L, 5L, pageable));

        verify(productImageRepository, never()).findByProductIdOrderBySortOrderAsc(anyLong(), any(Pageable.class));
    }

    @Test
    void deleteProductImages_shouldDeleteProductImages() {
        ProductImage productImage = defaultImage();

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(productImage));

        productImageService.deleteProductImages(1L, 5L, 1L);

        verify(productImageRepository).delete(productImage);
    }

    @Test
    void deleteProductImages_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productImageService.deleteProductImages(1L, 5L, 1L));

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void deleteProductImages_shouldThrowException_whenProductBelongsToDifferentStore() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThrows(ProductNotFoundException.class,
                () -> productImageService.deleteProductImages(99L, 5L, 1L));

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void deleteProductImages_shouldThrowException_whenProductImagesNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class,
                () -> productImageService.deleteProductImages(1L, 5L, 1L));

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void deleteProductImages_shouldThrowException_whenImagesBelongsToDifferentProduct() {
        ProductImage productImage = defaultImage();

        Product otherProduct = new Product();
        otherProduct.setId(2L);
        productImage.setProduct(otherProduct);

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(productImage));

        assertThrows(ImageNotFoundException.class,
                () -> productImageService.deleteProductImages(1L, 5L, 1L));

        verify(productImageRepository, never()).delete(any());
    }
}
