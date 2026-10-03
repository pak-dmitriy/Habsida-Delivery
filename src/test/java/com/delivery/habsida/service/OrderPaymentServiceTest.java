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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
        orderPayment.setStatus(PaymentStatus.UNPAID);
        orderPayment.setMethod(PaymentMethod.CASH);
        orderPayment.setId(1L);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setOrderPayment(orderPayment);
        transaction.setStatus(TransactionStatus.SUCCESS);
    }

    @Test
    void getOrderPayment_success() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));

        OrderPaymentDto response = orderPaymentService.getOrderPayment(1L, 1L);
        assertEquals(PaymentStatus.UNPAID, response.status());
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
                PaymentStatus.PAID,
                PaymentMethod.CASH
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
                PaymentStatus.PAID,
                PaymentMethod.CASH
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
                PaymentStatus.UNPAID,
                PaymentMethod.CASH
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
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        assertThrows(PaymentNotFoundException.class,
                () -> orderPaymentService.updateOrderPayment(1L, 1L, request));
    }

    @Test
    void updateOrderPayment_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.empty());
        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        assertThrows(OrderNotFoundException.class,
                () -> orderPaymentService.updateOrderPayment(1L, 1L, request));
    }

    @Test
    void updateOrderPayment_shouldThrowException_whenTransactionBelongsToAnotherOrder() {

        OrderPayment otherPayment = new OrderPayment();
        otherPayment.setId(99L);
        otherPayment.setStatus(PaymentStatus.PAID);
        otherPayment.setMethod(PaymentMethod.CASH);
        otherPayment.setOrder(order);

        Transaction otherTransaction = new Transaction();
        otherTransaction.setId(2L);
        otherTransaction.setStatus(TransactionStatus.SUCCESS);
        otherTransaction.setAmount(new BigDecimal(10.00));
        otherTransaction.setOrderPayment(otherPayment);

        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(otherTransaction));

        OrderPaymentRequest request = new OrderPaymentRequest(
                2L,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );

        assertThrows(TransactionNotFoundException.class,
                () -> orderPaymentService.updateOrderPayment(1L, 1L, request));
    }

    @Test
    void updateOrderPayment_shouldThrowException_whenTransactionStatusNotPaid() {

        OrderPayment failedPayment = new OrderPayment();
        failedPayment.setId(1L);
        failedPayment.setStatus(PaymentStatus.PAID);
        failedPayment.setMethod(PaymentMethod.CASH);
        failedPayment.setOrder(order);

        Transaction failedTransaction = new Transaction();
        failedTransaction.setId(1L);
        failedTransaction.setStatus(TransactionStatus.FAILED);
        failedTransaction.setAmount(new BigDecimal(10.00));
        failedTransaction.setOrderPayment(failedPayment);

        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(failedPayment));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(failedTransaction));

        OrderPaymentRequest request = new OrderPaymentRequest(
                1L,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );

        TransactionNotFoundException ex = assertThrows(TransactionNotFoundException.class,
                () -> orderPaymentService.updateOrderPayment(1L, 1L, request));
        assertEquals("Transaction not paid", ex.getMessage());
    }

    @Test
    void updateOrderPayment_shouldKeepPaidAt_whenStatusAlreadyPaid() {

        LocalDateTime expectedPaidAt = LocalDateTime.of(2026, 10, 2, 20, 24, 0);

        OrderPayment updatedPayment = new OrderPayment();
        updatedPayment.setId(1L);
        updatedPayment.setStatus(PaymentStatus.PAID);
        updatedPayment.setMethod(PaymentMethod.CASH);
        updatedPayment.setPaidAt(expectedPaidAt);
        updatedPayment.setOrder(order);

        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(updatedPayment));

        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.PAID,
                PaymentMethod.CASH
        );
        orderPaymentService.updateOrderPayment(1L, 1L, request);
        assertEquals(expectedPaidAt, updatedPayment.getPaidAt());
    }

    @Test
    void updateOrderPayment_shouldResetPaidAt_whenStatusChangedToRefunded() {

        LocalDateTime expectedPaidAt = LocalDateTime.of(2026, 10, 2, 20, 24, 0);

        OrderPayment updatedPayment = new OrderPayment();
        updatedPayment.setId(1L);
        updatedPayment.setStatus(PaymentStatus.PAID);
        updatedPayment.setMethod(PaymentMethod.CASH);
        updatedPayment.setPaidAt(expectedPaidAt);
        updatedPayment.setOrder(order);

        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(updatedPayment));

        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.REFUNDED,
                PaymentMethod.CASH
        );
        orderPaymentService.updateOrderPayment(1L, 1L, request);
        assertNull(updatedPayment.getPaidAt());
    }

    @Test
    void updateOrderPayment_shouldUpdateMethod_whenPaymentMethodChanged() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));

        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.PAID,
                PaymentMethod.ONLINE
        );
        orderPaymentService.updateOrderPayment(1L, 1L, request);
        assertEquals(PaymentMethod.ONLINE, orderPayment.getMethod());
    }

    @Test
    void updateOrderPayment_shouldKeepMethod_whenPaymentMethodNotChanged() {
        when(orderRepository.findByIdAndStoreId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderPaymentRepository.findByOrderId(1L)).thenReturn(Optional.of(orderPayment));

        OrderPaymentRequest request = new OrderPaymentRequest(
                null,
                PaymentStatus.PAID,
                null
        );
        orderPaymentService.updateOrderPayment(1L, 1L, request);
        assertEquals(PaymentMethod.CASH, orderPayment.getMethod());
    }
}