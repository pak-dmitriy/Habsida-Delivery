package com.delivery.habsida.controller;

import com.delivery.habsida.dto.OrderPaymentDto;
import com.delivery.habsida.dto.OrderPaymentRequest;
import com.delivery.habsida.service.OrderPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/orders/{orderId}/payment")
public class OrderPaymentController {
    private final OrderPaymentService orderPaymentService;

    public OrderPaymentController(OrderPaymentService orderPaymentService) {
        this.orderPaymentService = orderPaymentService;
    }

    @GetMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<OrderPaymentDto> getOrderPayment(@PathVariable Long storeId,
                                           @PathVariable Long orderId) {
        OrderPaymentDto result = orderPaymentService.getOrderPayment(storeId, orderId);
        return ResponseEntity.ok(result);
    }

    @PutMapping
    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    public ResponseEntity<OrderPaymentDto> updateOrderPayment(@PathVariable Long storeId,
                                                              @PathVariable Long orderId,
                                                              @Valid @RequestBody OrderPaymentRequest request) {
        OrderPaymentDto updated = orderPaymentService.updateOrderPayment(storeId, orderId, request);
        return ResponseEntity.ok(updated);
    }
}
