package com.delivery.habsida.repository;

import com.delivery.habsida.entity.Order;
import com.delivery.habsida.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    Optional<OrderPayment> findByOrderId(Long orderId);
}
