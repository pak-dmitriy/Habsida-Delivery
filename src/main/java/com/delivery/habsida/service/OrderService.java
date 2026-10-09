package com.delivery.habsida.service;

import com.delivery.habsida.dto.*;
import com.delivery.habsida.entity.*;
import com.delivery.habsida.exception.*;
import com.delivery.habsida.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.delivery.habsida.entity.OrderStatus.*;

@Service
public class OrderService {
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    private final OrderItemModifierRepository orderItemModifierRepository;
    private final ProductModifierGroupRepository productModifierGroupRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final StoreDeliverySettingRepository storeDeliverySettingRepository;
    private final StoreDeliveryAreaRepository storeDeliveryAreaRepository;
    private final StoreDeliveryRestrictionRepository storeDeliveryRestrictionRepository;

    public OrderService(OrderRepository orderRepository,
                        StoreRepository storeRepository,
                        CustomerRepository customerRepository,
                        CustomerAddressRepository customerAddressRepository,
                        ProductRepository productRepository,
                        OrderItemRepository orderItemRepository,
                        ModifierOptionRepository modifierOptionRepository,
                        OrderItemModifierRepository orderItemModifierRepository,
                        ProductModifierGroupRepository productModifierGroupRepository,
                        OrderPaymentRepository orderPaymentRepository,
                        StoreDeliverySettingRepository storeDeliverySettingRepository,
                        StoreDeliveryAreaRepository storeDeliveryAreaRepository,
                        StoreDeliveryRestrictionRepository storeDeliveryRestrictionRepository) {
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.modifierOptionRepository = modifierOptionRepository;
        this.orderItemModifierRepository = orderItemModifierRepository;
        this.productModifierGroupRepository = productModifierGroupRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.storeDeliverySettingRepository = storeDeliverySettingRepository;
        this.storeDeliveryAreaRepository = storeDeliveryAreaRepository;
        this.storeDeliveryRestrictionRepository = storeDeliveryRestrictionRepository;
    }

    private StoreDeliveryArea getDistrictOrThrow(String districtName, Long storeId) {
        return storeDeliveryAreaRepository.findByDistrictNameAndStoreId(districtName, storeId)
                .orElseThrow(() -> new StoreDeliveryAreaNotFoundException("Area not found"));
    }

    @Transactional
    public OrderDTO createOrder(Long storeId, OrderCreateRequest orderCreateRequest) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));
        Customer customer = customerRepository.findById(orderCreateRequest.customerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        CustomerAddress customerAddress = customerAddressRepository.findById(orderCreateRequest.customerAddressId())
                .orElseThrow(() -> new CustomerAddressNotFoundException("Customer address not found"));
        if (!customerAddress.getCustomer().getId().equals(customer.getId())) {
            throw new CustomerAddressNotFoundException("Customer address not found");
        }
        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;


        List<Product> products = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderCreateRequest.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            products.add(product);
            if (!product.getStore().getId().equals(storeId)) {
                throw new ProductNotFoundException("Product not found");
            }
            if (product.getStatus() != ProductStatus.AVAILABLE) {
                throw new ProductNotAvailableException("Product not available");
            }
            if (itemRequest.quantity() < product.getMinQuantity() || itemRequest.quantity() > product.getMaxQuantity()) {
                throw new InvalidQuantityException("Quantity must be between: " + product.getMinQuantity() + " and " + product.getMaxQuantity());
            }

            List<Integer> stockResult = productRepository.decreaseStockAndGet(product.getId(), itemRequest.quantity());
            if (stockResult.isEmpty()) {
                throw new InsufficientStockException("Insufficient stock");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setDiscountPercent(product.getDiscountPercent());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            BigDecimal unitDiscount = product.getPrice().multiply(BigDecimal.valueOf(product.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal itemDiscount = unitDiscount.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            orderItem.setDiscountAmount(itemDiscount);
            discountTotal = discountTotal.add(itemDiscount);
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
            orderSubtotal = orderSubtotal.add(subtotal);
        }

        BigDecimal deliveryFee;
        StoreDeliverySetting deliverySetting = null;
        if (orderCreateRequest.type() == OrderType.DELIVERY) {
           deliverySetting = storeDeliverySettingRepository.findByStoreId(storeId)
                    .orElseThrow(() -> new StoreDeliverySettingNotFoundException("Delivery setting not found"));
            if (!deliverySetting.getDeliveryEnabled()) {
                throw new DeliveryNotAvailableException("Delivery not available");
            }

            if (storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(customerAddress.getCity(), storeId) ) {
                throw new DeliveryNotAvailableException("Delivery not available in this zone");
            }

            StoreDeliveryArea deliveryArea = getDistrictOrThrow(customerAddress.getCity(), storeId);
            deliveryFee = deliveryArea.getDeliveryFee();
        } else {
            deliveryFee = BigDecimal.ZERO;
        }

        BigDecimal total = orderSubtotal.add(deliveryFee).subtract(discountTotal);

        Order order = new Order();
        order.setStore(store);
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);
        order.setDeliveryCity(customerAddress.getCity());
        order.setDeliveryStreet(customerAddress.getStreet());
        order.setDeliveryHouse(customerAddress.getHouse());
        order.setDeliveryApartment(customerAddress.getApartment());
        order.setOrderType(orderCreateRequest.type());
        order.setCustomerNote(orderCreateRequest.customerNote());
        order.setOrderStatus(CREATED);
        order.setSubTotal(orderSubtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountTotal(discountTotal);
        order.setTotal(total);
        Long orderNumber = storeRepository.incrementAndGetOrderNumber(storeId).get(0);
        order.setOrderNumber(String.valueOf(orderNumber));
        orderRepository.save(order);

        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem item = orderItems.get(i);
            Product product = products.get(i);
            OrderItemRequest itemRequest = orderCreateRequest.items().get(i);

            item.setOrder(order);
            orderItemRepository.save(item);

            BigDecimal modifiersTotal = BigDecimal.ZERO;

            Map<Long, Integer> selectCount = new HashMap<>();
            if (itemRequest.modifierOptionIds() != null) {
                for (Long optionId : itemRequest.modifierOptionIds()) {
                    ModifierOption modifierOption = modifierOptionRepository.findById(optionId)
                            .orElseThrow(() -> new ModifierOptionNotFoundException("Option not found"));

                    modifiersTotal = modifiersTotal.add(modifierOption.getPriceDelta());
                    ModifierGroup modifierGroup = modifierOption.getModifierGroup();

                    int current = selectCount.getOrDefault(modifierGroup.getId(), 0);
                    selectCount.put(modifierGroup.getId(), current + 1);
                    if (!productModifierGroupRepository.existsByProductIdAndModifierGroupId(product.getId(), modifierGroup.getId()))
                        throw new ModifierOptionNotFoundException("Option not found");

                    OrderItemModifier orderItemModifier = new OrderItemModifier();
                    orderItemModifier.setOptionName(modifierOption.getName());
                    orderItemModifier.setPriceDelta(modifierOption.getPriceDelta());
                    orderItemModifier.setModifierOption(modifierOption);
                    orderItemModifier.setOrderItem(item);
                    orderItemModifierRepository.save(orderItemModifier);
                }
            }

            for (ProductModifierGroup productModifierGroup : productModifierGroupRepository.findByProductId(product.getId())) {
                ModifierGroup group = productModifierGroup.getModifierGroup();
                int selected = selectCount.getOrDefault(group.getId(), 0);
                if (selected < group.getMinSelect()) {
                    throw new InvalidModifierSelectionException("Select at least " + group.getMinSelect() + " option(s) from group: " + group.getName());
                }
                if (selected > group.getMaxSelect()) {
                    throw new InvalidModifierSelectionException("Select at max " + group.getMaxSelect() + " option(s) from group: " + group.getName());
                }
                if (group.isRequired() && selected == 0) {
                    throw new InvalidModifierSelectionException("You must select at least 1 option" + " from group: " + group.getName());
                }
            }
            BigDecimal subtotal = product.getPrice().add(modifiersTotal).multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);
            orderItemRepository.save(item);
        }
        BigDecimal finalOrderSubtotal = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            finalOrderSubtotal = finalOrderSubtotal.add(item.getSubtotal());
        }

        if(deliverySetting != null && finalOrderSubtotal.compareTo(deliverySetting.getMinOrderPrice()) < 0) {
            throw new MinOrderPriceNotReachedException("Order total is below the minimum: " + deliverySetting.getMinOrderPrice());
        }

        if(deliverySetting != null && finalOrderSubtotal.compareTo(deliverySetting.getFreeDeliveryThreshold()) >= 0
                && deliverySetting.getFreeDeliveryThreshold().compareTo(BigDecimal.ZERO) > 0) {
            deliveryFee = BigDecimal.ZERO;
        }
        order.setDeliveryFee(deliveryFee);

        BigDecimal finalTotal = finalOrderSubtotal.add(deliveryFee).subtract(discountTotal);
        order.setSubTotal(finalOrderSubtotal);
        order.setTotal(finalTotal);
        orderRepository.save(order);

        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setStatus(PaymentStatus.UNPAID);
        orderPayment.setMethod(orderCreateRequest.method());
        orderPayment.setOrder(order);
        orderPaymentRepository.save(orderPayment);

        List<OrderItemDto> itemDtos = orderItems.stream()
                .map(item -> {
                    List<OrderItemModifier> modifiersEntities = orderItemModifierRepository.findByOrderItemId(item.getId());
                    List<OrderItemModifierDto> modifierDtos = modifiersEntities.stream()
                            .map(OrderItemModifierDto::from)
                            .toList();
                    return OrderItemDto.from(item, modifierDtos);
                }).toList();
        return OrderDTO.from(order, itemDtos);
    }

    @Transactional
    public OrderDTO acceptOrder(Long storeId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (!order.getStore().getId().equals(storeId)) {
            throw new OrderNotFoundException("Order not found");
        }
        if (!OrderStatus.canTransition(order.getOrderStatus(), OrderStatus.ACCEPTED)) {
            throw new InvalidOrderStatusTransitionException("Cannot transition from " + order.getOrderStatus() + " to ACCEPTED");
        }
        order.setOrderStatus(ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> itemDtos = items.stream()
                .map(item -> {
                    List<OrderItemModifier> modifierEntities = orderItemModifierRepository.findByOrderItemId(item.getId());
                    List<OrderItemModifierDto> modifierDtos = modifierEntities.stream()
                            .map(OrderItemModifierDto::from).toList();
                    return OrderItemDto.from(item, modifierDtos);
                }).toList();
        return OrderDTO.from(order, itemDtos);
    }

    @Transactional
    public OrderDTO rejectOrder(Long storeId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (!order.getStore().getId().equals(storeId)) {
            throw new OrderNotFoundException("Order not found");
        }
        if (!OrderStatus.canTransition(order.getOrderStatus(), OrderStatus.REJECTED)) {
            throw new InvalidOrderStatusTransitionException("Cannot transition from " + order.getOrderStatus() + " to REJECTED");
        }
        order.setOrderStatus(REJECTED);
        order.setRejectedAt(LocalDateTime.now());
        order.setRejectReason(reason);
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> itemDtos = items.stream()
                .map(item -> {
                    List<OrderItemModifier> modifierEntities = orderItemModifierRepository.findByOrderItemId(item.getId());
                    List<OrderItemModifierDto> modifierDtos = modifierEntities.stream()
                            .map(OrderItemModifierDto::from).toList();
                    return OrderItemDto.from(item, modifierDtos);
                }).toList();
        return OrderDTO.from(order, itemDtos);
    }

    @Transactional
    public OrderDTO cancelOrder(Long storeId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (!order.getStore().getId().equals(storeId)) {
            throw new OrderNotFoundException("Order not found");
        }
        if (!OrderStatus.canTransition(order.getOrderStatus(), OrderStatus.CANCELLED)) {
            throw new InvalidOrderStatusTransitionException("Cannot transition from " + order.getOrderStatus() + " to CANCELLED");
        }
        order.setOrderStatus(CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> itemDtos = items.stream()
                .map(item -> {
                    List<OrderItemModifier> modifierEntities = orderItemModifierRepository.findByOrderItemId(item.getId());
                    List<OrderItemModifierDto> modifierDtos = modifierEntities.stream()
                            .map(OrderItemModifierDto::from).toList();
                    return OrderItemDto.from(item, modifierDtos);
                }).toList();
        return OrderDTO.from(order, itemDtos);
    }

    @Transactional
    public OrderDTO startOrder(Long storeId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (!order.getStore().getId().equals(storeId)) {
            throw new OrderNotFoundException("Order not found");
        }
        if (!OrderStatus.canTransition(order.getOrderStatus(), OrderStatus.IN_PROGRESS)) {
            throw new InvalidOrderStatusTransitionException("Cannot transition from " + order.getOrderStatus() + " to IN_PROGRESS");
        }
        order.setOrderStatus(IN_PROGRESS);
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> itemDtos = items.stream()
                .map(item -> {
                    List<OrderItemModifier> modifierEntities = orderItemModifierRepository.findByOrderItemId(item.getId());
                    List<OrderItemModifierDto> modifierDtos = modifierEntities.stream()
                            .map(OrderItemModifierDto::from).toList();
                    return OrderItemDto.from(item, modifierDtos);
                }).toList();
        return OrderDTO.from(order, itemDtos);
    }

    @Transactional
    public OrderDTO completeOrder(Long storeId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (!order.getStore().getId().equals(storeId)) {
            throw new OrderNotFoundException("Order not found");
        }
        if (!OrderStatus.canTransition(order.getOrderStatus(), OrderStatus.COMPLETED)) {
            throw new InvalidOrderStatusTransitionException("Cannot transition from " + order.getOrderStatus() + " to COMPLETED");
        }
        order.setOrderStatus(COMPLETED);
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> itemDtos = items.stream()
                .map(item -> {
                    List<OrderItemModifier> modifierEntities = orderItemModifierRepository.findByOrderItemId(item.getId());
                    List<OrderItemModifierDto> modifierDtos = modifierEntities.stream()
                            .map(OrderItemModifierDto::from).toList();
                    return OrderItemDto.from(item, modifierDtos);
                }).toList();
        return OrderDTO.from(order, itemDtos);
    }

    @Transactional
    public List<OrderDTO> getNewOrders(Long storeId) {
        List<Order> orders = orderRepository.findByStoreIdAndOrderStatus(storeId, CREATED);
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .toList();
        List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
        List<Long> itemIds = allItems.stream()
                .map(OrderItem::getId)
                .toList();
        List<OrderItemModifier> allModifiers = orderItemModifierRepository.findByOrderItemIdIn(itemIds);
        Map<Long, List<OrderItemModifier>> modifiersByItemId = allModifiers.stream()
                .collect(Collectors.groupingBy(modifier -> modifier.getOrderItem().getId()));
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    List<OrderItemDto> itemDtos = items.stream()
                            .map(item -> {
                                List<OrderItemModifier> modifierEntities = modifiersByItemId.getOrDefault(item.getId(), List.of());
                                List<OrderItemModifierDto> modifierDtos = modifierEntities.stream()
                                        .map(OrderItemModifierDto::from)
                                        .toList();
                                return OrderItemDto.from(item, modifierDtos);
                            }).toList();
                    return OrderDTO.from(order, itemDtos);
                })
                .toList();
    }
}
