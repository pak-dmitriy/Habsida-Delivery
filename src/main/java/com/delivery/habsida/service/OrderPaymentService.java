package com.delivery.habsida.service;

import com.delivery.habsida.dto.OrderPaymentDto;
import com.delivery.habsida.dto.OrderPaymentRequest;
import com.delivery.habsida.entity.Order;
import com.delivery.habsida.entity.OrderPayment;
import com.delivery.habsida.entity.PaymentStatus;
import com.delivery.habsida.entity.Transaction;
import com.delivery.habsida.exception.OrderNotFoundException;
import com.delivery.habsida.exception.PaymentNotFoundException;
import com.delivery.habsida.exception.TransactionNotFoundException;
import com.delivery.habsida.repository.OrderPaymentRepository;
import com.delivery.habsida.repository.OrderRepository;
import com.delivery.habsida.repository.TransactionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class OrderPaymentService {
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;

    public OrderPaymentService(OrderPaymentRepository orderPaymentRepository,
                               OrderRepository orderRepository,
                               TransactionRepository transactionRepository) {
        this.orderPaymentRepository = orderPaymentRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
    }

    private Order getOrderOrThrow(Long storeId, Long id) {
        return orderRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    private OrderPayment getPaymentOrThrow(Long orderId) {
        return orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    }

    private Transaction getTransactionOrThrow(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public OrderPaymentDto getOrderPayment(Long storeId, Long orderId) {
        getOrderOrThrow(storeId, orderId);
        OrderPayment orderPayment = getPaymentOrThrow(orderId);

        return OrderPaymentDto.from(orderPayment);
    }

    @Transactional
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public OrderPaymentDto updateOrderPayment(Long storeId, Long orderId, OrderPaymentRequest request) {
        getOrderOrThrow(storeId, orderId);

        OrderPayment result = getPaymentOrThrow(orderId);
        result.setStatus(request.status());

        if (request.status() == PaymentStatus.PAID) {
            result.setPaidAt(LocalDateTime.now());
        }

        if (request.transactionId() != null) {
            Transaction transResult = getTransactionOrThrow(request.transactionId());
            result.setTransaction(transResult);
        }
        return OrderPaymentDto.from(result);
    }
}
