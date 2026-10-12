package com.delivery.habsida.controller;

import com.delivery.habsida.dto.OrderCreateRequest;
import com.delivery.habsida.dto.OrderResponse;
import com.delivery.habsida.dto.OrderRejectRequest;
import com.delivery.habsida.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/stores/{storeId}/orders")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long storeId,
                                                     @RequestBody @Valid OrderCreateRequest orderCreateRequest) {
        OrderResponse created = orderService.createOrder(storeId, orderCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/stores/{storeId}/orders/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse accepted = orderService.acceptOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(accepted);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/stores/{storeId}/orders/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long storeId, @PathVariable Long orderId, @RequestBody @Valid OrderRejectRequest orderRejectRequest) {
        OrderResponse rejected = orderService.rejectOrder(storeId, orderId, orderRejectRequest.reason());
        return ResponseEntity.status(HttpStatus.OK).body(rejected);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/stores/{storeId}/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse cancelled = orderService.cancelOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(cancelled);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/stores/{storeId}/orders/{orderId}/start")
    public ResponseEntity<OrderResponse> startOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse started = orderService.startOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(started);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/stores/{storeId}/orders/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse completed = orderService.completeOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(completed);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @GetMapping("/stores/{storeId}/orders/new")
    public ResponseEntity<List<OrderResponse>> newOrder(@PathVariable Long storeId) {
        List<OrderResponse> newOrder = orderService.getNewOrders(storeId);
        return ResponseEntity.status(HttpStatus.OK).body(newOrder);
    }
}
