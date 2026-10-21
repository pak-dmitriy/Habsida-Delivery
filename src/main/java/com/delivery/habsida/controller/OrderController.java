package com.delivery.habsida.controller;

import com.delivery.habsida.dto.OrderCreateRequest;
import com.delivery.habsida.dto.OrderRejectRequest;
import com.delivery.habsida.dto.OrderResponse;
import com.delivery.habsida.entity.OrderStatus;
import com.delivery.habsida.service.OrderService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long storeId,
                                                     @RequestBody @Valid OrderCreateRequest orderCreateRequest) {
        OrderResponse created = orderService.createOrder(storeId, orderCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse accepted = orderService.acceptOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(accepted);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long storeId, @PathVariable Long orderId, @RequestBody @Valid OrderRejectRequest orderRejectRequest) {
        OrderResponse rejected = orderService.rejectOrder(storeId, orderId, orderRejectRequest.reason());
        return ResponseEntity.status(HttpStatus.OK).body(rejected);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse cancelled = orderService.cancelOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(cancelled);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/{orderId}/start")
    public ResponseEntity<OrderResponse> startOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse started = orderService.startOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(started);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        OrderResponse completed = orderService.completeOrder(storeId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(completed);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @GetMapping("/new")
    public Page<OrderResponse> newOrder(@PathVariable Long storeId, @ParameterObject Pageable pageable) {
        return orderService.getNewOrders(storeId, pageable);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long storeId, @PathVariable Long orderId) {
        return orderService.getOrder(storeId, orderId);
    }

    @PreAuthorize("@storeAccessGuard.canAccessStore(authentication, #storeId)")
    @GetMapping
    public Page<OrderResponse> getOrders(@PathVariable Long storeId,
                                         @RequestParam(required = false) OrderStatus status,
                                         @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getOrders(storeId, status, pageable);
    }
}
