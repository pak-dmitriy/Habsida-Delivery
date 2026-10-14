package com.delivery.habsida.repository;

import com.delivery.habsida.entity.OrderItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifier, Long> {
    List<OrderItemModifier> findByOrderItemId(Long orderItemId);
    List<OrderItemModifier> findByOrderItemIdIn(List<Long> orderItemIds);
 }
