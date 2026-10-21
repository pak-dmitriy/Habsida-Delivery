package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Order;
import com.delivery.habsida.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByStoreId(Long storeId);

    Page<Order> findByStoreIdAndOrderStatus(Long storeId, OrderStatus orderStatus, Pageable pageable);

    Optional<Order> findByIdAndStoreId(Long id, Long storeId);

    Page<Order> findByStoreId(Long storeId, Pageable pageable);
}
