package com.delivery.habsida.service;

import com.delivery.habsida.dto.OrderPaymentDto;
import com.delivery.habsida.dto.OrderPaymentRequest;
import com.delivery.habsida.entity.*;
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
import java.util.Objects;

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

    public OrderPaymentDto getOrderPayment(Long storeId, Long orderId) {
        getOrderOrThrow(storeId, orderId);
        OrderPayment orderPayment = getPaymentOrThrow(orderId);

        return OrderPaymentDto.from(orderPayment);
    }

    @Transactional
    public OrderPaymentDto updateOrderPayment(Long storeId, Long orderId, OrderPaymentRequest request) {
        getOrderOrThrow(storeId, orderId);

        OrderPayment orderPayment = getPaymentOrThrow(orderId);
        PaymentStatus oldStatus = orderPayment.getStatus();


        if (oldStatus != PaymentStatus.PAID && request.status() == PaymentStatus.PAID) {
            orderPayment.setPaidAt(LocalDateTime.now());
        }
        if(oldStatus == PaymentStatus.PAID && request.status() != PaymentStatus.PAID) {
            orderPayment.setPaidAt(null);
        }
        if(request.method() != null) {
           orderPayment.setMethod(request.method());
        }
        orderPayment.setStatus(request.status());

        if (request.transactionId() != null) {
            Transaction transResult = getTransactionOrThrow(request.transactionId());
            if (!transResult.getOrderPayment().getId().equals(orderPayment.getId()) ) {
                throw new TransactionNotFoundException("Transaction not found");
            }
            if(transResult.getStatus() != TransactionStatus.SUCCESS) {
                throw new TransactionNotFoundException("Transaction not paid");
            }

            orderPayment.setTransaction(transResult);
        }
        return OrderPaymentDto.from(orderPayment);
    }
}
