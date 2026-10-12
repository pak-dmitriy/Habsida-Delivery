package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Order;
import com.delivery.habsida.entity.OrderStatus;
import com.delivery.habsida.entity.OrderType;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        Long storeId,
        Long customerId,
        Long customerAddressId,
        OrderType orderType,
        OrderStatus orderStatus,
        String rejectReason,
        String customerNote,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal discountTotal,
        BigDecimal total,
        List<OrderItemResponse> orderItems,
        String orderNumber,
        String currency,
        String deliveryCity,
        String deliveryStreet,
        String deliveryHouse,
        String deliveryApartment
) {

    public static OrderResponse from(Order order, List<OrderItemResponse> items) {
        return new OrderResponse(
                order.getId(),
                order.getStore().getId(),
                order.getCustomer().getId(),
                order.getCustomerAddress().getId(),
                order.getOrderType(),
                order.getOrderStatus(),
                order.getRejectReason(),
                order.getCustomerNote(),
                order.getSubTotal(),
                order.getDeliveryFee(),
                order.getDiscountTotal(),
                order.getTotal(),
                items,
                order.getOrderNumber(),
                order.getCurrency(),
                order.getDeliveryCity(),
                order.getDeliveryStreet(),
                order.getDeliveryHouse(),
                order.getDeliveryApartment()
        );
    }
}
