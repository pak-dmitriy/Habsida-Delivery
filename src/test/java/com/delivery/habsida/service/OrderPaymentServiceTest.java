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
import org.assertj.core.api.LocalDateTimeAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderPaymentServiceTest {
    @Mock
    private OrderPaymentRepository orderPaymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private OrderPaymentService orderPaymentService;

    private OrderPayment orderPayment;
    private Order order;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);

        orderPayment = new OrderPayment();
        orderPayment.setOrder(order);
        orderPayment.setStatus(PaymentStatus.PAID);
        orderPayment.setMethod(PaymentMethod.CASH);
        orderPayment.setId(1L);

        transaction = new Transaction();
        transaction.setId(1L);
    }

    @Test
    void getOrderPayment_success() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));

        OrderPaymentDto response = orderPaymentService.getOrderPayment(1L, 1L);
        assertEquals(PaymentStatus.PAID, response.status());
        assertEquals(PaymentMethod.CASH, response.method());
    }

    @Test
    void getOrderPayment_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderPaymentService.getOrderPayment(1L, 1L));
    }

    @Test
    void getOrderPayment_shouldThrowException_whenPaymentNotFound() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> orderPaymentService.getOrderPayment(1L, 1L));
    }

    @Test
    void updateOrderPayment_success() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID
        );

        OrderPaymentDto response = orderPaymentService.updateOrderPayment(1L, 1L, request);
        assertEquals(1L, response.transaction().id());
        assertEquals(PaymentStatus.PAID, response.status());
        assertNotNull(response.paidAt());
    }

    @Test
    void updateOrderPayment_shouldThrowException_whenTransactionNotFound() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID
        );
        assertThrows(TransactionNotFoundException.class,
                () -> orderPaymentService.updateOrderPayment(1L, 1L, request));
    }

    @Test
    void updateOrderPayment_shouldNotSetPaidAt_whenStatusIsNotPaid() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));
        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.UNPAID
        );
        OrderPaymentDto response = orderPaymentService.updateOrderPayment(1L, 1L, request);
        assertEquals(PaymentStatus.UNPAID, response.status());
        assertNull(response.paidAt());
        verify(transactionRepository, never()).findById(any());
    }

    @Test
    void updateOrderPayment_shouldThrowException_whenPaymentNotFound() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID
        );
        assertThrows(PaymentNotFoundException.class,
                () -> orderPaymentService.updateOrderPayment(1L, 1L, request));
    }

    @Test
    void updateOrderPayment_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());
        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID
        );
        assertThrows(OrderNotFoundException.class,
                ()-> orderPaymentService.updateOrderPayment(1L, 1L, request));
    }
}