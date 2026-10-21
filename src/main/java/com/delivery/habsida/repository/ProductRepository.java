package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query(value = "UPDATE products SET stock = stock - :quantity WHERE id = :productId AND stock >= :quantity RETURNING stock",
    nativeQuery = true)

    List<Integer> decreaseStockAndGet(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query(value = "UPDATE products SET stock = stock + :quantity WHERE id = :productId", nativeQuery = true)
    void increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndCategoryId(Long storeId, Long categoryId, Pageable pageable);

    Page<Product> findByStoreIdAndStatus(Long storeId, ProductStatus status, Pageable pageable);

    Page<Product> findByStoreIdAndCategoryIdAndStatus(Long storeId, Long categoryId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndStoreId(Long productId, Long storeId);
}
