package com.delivery.habsida.service;

import com.delivery.habsida.dto.OrderCreateRequest;
import com.delivery.habsida.dto.OrderDTO;
import com.delivery.habsida.dto.OrderItemRequest;
import com.delivery.habsida.entity.*;
import com.delivery.habsida.exception.*;
import com.delivery.habsida.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.delivery.habsida.entity.OrderType.DELIVERY;
import static com.delivery.habsida.entity.OrderType.PICKUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerAddressRepository customerAddressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ModifierOptionRepository modifierOptionRepository;
    @Mock
    private OrderItemModifierRepository orderItemModifierRepository;
    @Mock
    private ProductModifierGroupRepository productModifierGroupRepository;
    @Mock
    private OrderPaymentRepository orderPaymentRepository;
    @Mock
    private StoreDeliverySettingRepository storeDeliverySettingRepository;
    @Mock
    private StoreDeliveryAreaRepository storeDeliveryAreaRepository;
    @Mock
    private StoreDeliveryRestrictionRepository storeDeliveryRestrictionRepository;

    @InjectMocks
    private OrderService orderService;

    private Store store;
    private Customer customer;
    private CustomerAddress customerAddress;
    private Product product;
    private Order order;
    private ModifierOption modifierOption;
    private OrderItemModifier orderItemModifier;
    private ModifierGroup modifierGroup;
    private ProductModifierGroup productModifierGroup;
    private StoreDeliverySetting storeDeliverySetting;
    private StoreDeliveryArea storeDeliveryArea;
    private StoreDeliveryRestriction storeDeliveryRestriction;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        customer = new Customer();
        customer.setId(1L);

        customerAddress = new CustomerAddress();
        customerAddress.setId(1L);
        customerAddress.setCity("City");
        customerAddress.setCustomer(customer);

        order = new Order();
        order.setId(1L);
        order.setStore(store);
        order.setOrderStatus(OrderStatus.CREATED);
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        orderItemModifier = new OrderItemModifier();
        orderItemModifier.setId(1L);
        orderItemModifier.setOptionName("Double size");
        orderItemModifier.setPriceDelta(new BigDecimal("10.2"));

        storeDeliveryRestriction = new StoreDeliveryRestriction();
        storeDeliveryRestriction.setRestrictedArea("City");

        product = defaultProduct();
        modifierGroup = defaultModifierGroup();
        modifierOption = defaultOption(modifierGroup);
        productModifierGroup = attachGroupToProduct(product, modifierGroup);
    }

    private OrderCreateRequest defaultRequest() {
        return new OrderCreateRequest(
                DELIVERY,
                PaymentMethod.CASH,
                "Good",
                1L,
                1L,
                List.of(new OrderItemRequest(1L, 2, List.of(1L)))
        );
    }

    private StoreDeliverySetting defaultSetting() {
        StoreDeliverySetting setting = new StoreDeliverySetting();
        setting.setStore(store);
        setting.setDeliveryEnabled(true);
        setting.setDeliveryType(DeliveryType.SAME_DAY);
        setting.setMinOrderPrice(new BigDecimal("10.0"));
        setting.setFreeDeliveryThreshold(new BigDecimal("50.0"));
        setting.setMaxDistance(new BigDecimal("30.0"));
        setting.setDeliveryGeography(DeliveryGeography.ONLY_IN_THE_CITY);

        return setting;
    }

    private StoreDeliveryArea defaultArea() {
        StoreDeliveryArea area = new StoreDeliveryArea();
        area.setStore(store);
        area.setDistrictName("City");
        area.setDeliveryFee(new BigDecimal("10.0"));

        return area;
    }

    private Product defaultProduct() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("apple");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setStore(store);
        product1.setStatus(ProductStatus.AVAILABLE);
        product1.setMaxQuantity(10);
        product1.setMinQuantity(1);
        product1.setStock(100);

        return product1;
    }

    private ModifierGroup defaultModifierGroup() {
        ModifierGroup group = new ModifierGroup();
        group.setId(1L);
        group.setMaxSelect(1);

        return group;
    }

    private ModifierOption defaultOption(ModifierGroup group) {
        ModifierOption option = new ModifierOption();
        option.setId(1L);
        option.setName("Double size");
        option.setPriceDelta(new BigDecimal("10.2"));
        option.setModifierGroup(group);

        return option;
    }

    private ProductModifierGroup attachGroupToProduct(Product product, ModifierGroup group) {
        ProductModifierGroup productModifierGroup1 = new ProductModifierGroup();
        productModifierGroup1.setProduct(product);
        productModifierGroup1.setModifierGroup(group);

        return productModifierGroup1;
    }

    @Test
    void createOrder() {
        OrderCreateRequest request = defaultRequest();
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.findByProductId(1L)).thenReturn(List.of(productModifierGroup));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO result = orderService.createOrder(1L, request);

        assertEquals(new BigDecimal("50.40"), result.total());
        assertEquals(1, result.orderItems().size());
        assertEquals("apple", result.orderItems().get(0).productName());
        assertEquals("1", result.orderNumber());

        verify(orderPaymentRepository).save(any(OrderPayment.class));
    }

    @Test
    void createOrder_shouldThrowException_whenDeliveryNotAvailable() {
        storeDeliverySetting = defaultSetting();
        storeDeliverySetting.setDeliveryEnabled(false);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));

        assertThrows(DeliveryNotAvailableException.class,
                () -> orderService.createOrder(1L, defaultRequest()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrowException_whenCityInDeliveryRestriction() {
        storeDeliverySetting = defaultSetting();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId("City", 1L)).thenReturn(true);

        assertThrows(DeliveryNotAvailableException.class,
                () -> orderService.createOrder(1L, defaultRequest()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrowException_whereDeliveryAreaNotFound() {
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId("City", 1L)).thenReturn(Optional.empty());
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));

        assertThrows(StoreDeliveryAreaNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrowException_whenMinOrderPriceNotReached() {
        storeDeliveryArea = defaultArea();
        storeDeliverySetting = defaultSetting();
        storeDeliverySetting.setMinOrderPrice(new BigDecimal("100.0"));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryRestrictionRepository.existsByRestrictedAreaAndStoreId(customerAddress.getCity(), 1L)).thenReturn(false);
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(productModifierGroupRepository.findByProductId(1L)).thenReturn(List.of(productModifierGroup));

        assertThrows(MinOrderPriceNotReachedException.class,
                () -> orderService.createOrder(1L, defaultRequest()));

        verify(orderRepository).save(any());
    }

    @Test
    void creatOrder_success_whenDeliveryIsFree() {
        OrderCreateRequest request = defaultRequest();
        storeDeliverySetting = defaultSetting();
        storeDeliverySetting.setFreeDeliveryThreshold(new BigDecimal("40.40"));
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.findByProductId(1L)).thenReturn(List.of(productModifierGroup));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO result = orderService.createOrder(1L, request);

        assertEquals(new BigDecimal("40.40"), result.total());
        assertEquals(1, result.orderItems().size());
        assertEquals("apple", result.orderItems().get(0).productName());
        assertEquals("1", result.orderNumber());

        verify(orderPaymentRepository).save(any(OrderPayment.class));
    }

    @Test
    void creatOrder_success_whenDeliveryTypeIsPickUp() {
        OrderCreateRequest updateDeliveryType = new OrderCreateRequest(
                PICKUP,
                PaymentMethod.CASH,
                "Good",
                1L,
                1L,
                List.of(new OrderItemRequest(1L, 2, List.of(1L)))
        );

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.findByProductId(1L)).thenReturn(List.of(productModifierGroup));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);

        OrderDTO result = orderService.createOrder(1L, updateDeliveryType);

        assertEquals(new BigDecimal("40.40"), result.total());
        assertEquals(1, result.orderItems().size());
        assertEquals("apple", result.orderItems().get(0).productName());
        assertEquals("1", result.orderNumber());

        verify(storeDeliverySettingRepository, never()).findByStoreId(anyLong());
    }

    @Test
    void createOrder_shouldThrowException_whenDeliverySettingNotFound() {
        storeDeliverySetting = defaultSetting();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.empty());

        assertThrows(StoreDeliverySettingNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldCreateOrderItemModifier_whenModifierOptionSelected() {
        OrderCreateRequest request = defaultRequest();
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(orderItemModifierRepository.findByOrderItemId(any())).thenReturn(List.of(orderItemModifier));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO result = orderService.createOrder(1L, request);

        assertEquals(1, result.orderItems().get(0).orderItemModifiers().size());
        assertEquals("Double size", result.orderItems().get(0).orderItemModifiers().get(0).optionName());
        assertEquals(new BigDecimal("10.2"), result.orderItems().get(0).orderItemModifiers().get(0).priceDelta());
    }

    @Test
    void createOrder_shouldThrowException_whereModifierOptionNotFound() {
        OrderCreateRequest request = defaultRequest();
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.empty());
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        assertThrows(ModifierOptionNotFoundException.class,
                () -> orderService.createOrder(1L, request));
    }

    @Test
    void createOrder_shouldThrowException_whenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldThrowException_whenCustomerNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldThrowException_whenCustomerAddressNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerAddressNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldThrowException_whenProductNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldGenerateCorrectOrderNumber_whenStoreHasExistingOrders() {
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(6L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO result = orderService.createOrder(1L, defaultRequest());

        assertEquals("6", result.orderNumber());
    }

    @Test
    void createOrder_shouldThrowException_whenAddressBelongsToDifferentCustomer() {
        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(2L);
        customerAddress.setCustomer(anotherCustomer);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));

        assertThrows(CustomerAddressNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldThrowException_whenProductFromDifferentStore() {
        Store anotherStore = new Store();
        anotherStore.setId(2L);
        product.setStore(anotherStore);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ProductNotFoundException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldThrowException_whenProductNotAvailable() {
        product.setStatus(ProductStatus.PAUSED);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ProductNotAvailableException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void createOrder_shouldThrowException_whenQuantityOutOfRange() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderCreateRequest request = new OrderCreateRequest(
                DELIVERY,
                PaymentMethod.CASH,
                "Good",
                1L,
                1L,
                List.of(new OrderItemRequest(1L, 11, List.of(1L)))
        );

        assertThrows(InvalidQuantityException.class,
                () -> orderService.createOrder(1L, request));
    }

    @Test
    void createOrder_shouldThrowException_whenInsufficientStock() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of());

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(1L, defaultRequest()));
    }

    @Test
    void acceptOrder_shouldSucceed_whenTransitionIsValid() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());
        OrderDTO result = orderService.acceptOrder(1L, order.getId());
        assertEquals(OrderStatus.ACCEPTED, result.orderStatus());
    }

    @Test
    void acceptOrder_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.acceptOrder(1L, 1L));
    }

    @Test
    void acceptOrder_shouldThrowException_whenOrderBelongsToDifferentStore() {
        Store anotherStore = new Store();
        anotherStore.setId(2L);
        order.setStore(anotherStore);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderNotFoundException.class,
                () -> orderService.acceptOrder(1L, order.getId()));
    }

    @Test
    void acceptOrder_shouldThrowException_whenTransitionIsInvalid() {
        order.setOrderStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.acceptOrder(1L, order.getId()));
    }

    @Test
    void rejectOrder_shouldSucceed_whenTransitionIsValid() {
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());

        OrderDTO result = orderService.rejectOrder(1L, order.getId(), "Out of stock");

        assertEquals(OrderStatus.REJECTED, result.orderStatus());
    }

    @Test
    void rejectOrder_shouldThrowException_whenTransitionIsInvalid() {
        order.setOrderStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.rejectOrder(1L, order.getId(), "Out of stock"));
    }

    @Test
    void cancelOrder_shouldSucceed_whenTransitionIsValid() {
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());

        OrderDTO result = orderService.cancelOrder(1L, order.getId());

        assertEquals(OrderStatus.CANCELLED, result.orderStatus());
    }

    @Test
    void cancelOrder_shouldThrowException_whenTransitionIsInvalid() {
        order.setOrderStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.cancelOrder(1L, order.getId()));
    }

    @Test
    void startOrder_shouldSucceed_whenTransitionIsValid() {
        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());

        OrderDTO result = orderService.startOrder(1L, order.getId());

        assertEquals(OrderStatus.IN_PROGRESS, result.orderStatus());
    }

    @Test
    void startOrder_shouldThrowException_whenTransitionIsInvalid() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.startOrder(1L, order.getId()));
    }

    @Test
    void completeOrder_shouldSucceed_whenTransitionIsValid() {
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());

        OrderDTO result = orderService.completeOrder(1L, order.getId());

        assertEquals(OrderStatus.COMPLETED, result.orderStatus());
    }

    @Test
    void completeOrder_shouldThrowException_whenTransitionIsInvalid() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.completeOrder(1L, order.getId()));
    }

    @Test
    void getNewOrders_shouldReturnOrderList() {
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findByStoreIdAndOrderStatus(1L, OrderStatus.CREATED))
                .thenReturn(List.of(order));
        when(orderItemRepository.findByOrderIdIn(List.of(order.getId())))
                .thenReturn(List.of());

        List<OrderDTO> result = orderService.getNewOrders(1L);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.CREATED, result.get(0).orderStatus());
    }

    @Test
    void cancelOrder_shouldSucceed_whenOrderIsAccepted() {
        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());

        OrderDTO result = orderService.cancelOrder(1L, order.getId());

        assertEquals(OrderStatus.CANCELLED, result.orderStatus());
    }

    @Test
    void acceptOrder_shouldPropagateException_whenVersionConflictOccurs() {
        order.setCustomer(customer);
        order.setCustomerAddress(customerAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, order.getId()));

        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> orderService.acceptOrder(1L, order.getId()));
    }

    @Test
    void createOrder_shouldApplyDiscount_whenProductHasDiscount() {
        product.setDiscountPercent(20);
        OrderCreateRequest request = defaultRequest();
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO result = orderService.createOrder(1L, request);

        assertEquals(new BigDecimal("4.00"), result.discountTotal());
        assertEquals(new BigDecimal("46.40"), result.total());
    }

    @Test
    void createOrder_shouldHaveZeroDiscount_whenProductHasNoDiscount() {
        OrderCreateRequest request = defaultRequest();
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO result = orderService.createOrder(1L, request);

        assertEquals(new BigDecimal("0.00"), result.discountTotal());
        assertEquals(new BigDecimal("50.40"), result.total());
    }

    @Test
    void createOrder_shouldRoundDiscountToTwoDecimals_whenDivisionIsNotExact() {
        product.setPrice(new BigDecimal("10.01"));
        product.setDiscountPercent(33);
        storeDeliverySetting = defaultSetting();
        storeDeliveryArea = defaultArea();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(modifierOptionRepository.findById(1L)).thenReturn(Optional.of(modifierOption));
        when(productModifierGroupRepository.existsByProductIdAndModifierGroupId(1L, 1L)).thenReturn(true);
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));
        OrderDTO result = orderService.createOrder(1L, defaultRequest());

        assertEquals(new BigDecimal("6.60"), result.discountTotal());
    }

    @Test
    void createOrder_shouldApplySameTotalDiscount_regardlessOfItemSplitting() {
        product.setPrice(new BigDecimal("0.01"));
        product.setDiscountPercent(50);
        storeDeliverySetting = defaultSetting();
        storeDeliverySetting.setMinOrderPrice(new BigDecimal("0"));
        storeDeliveryArea = defaultArea();

        OrderCreateRequest oneLinesRequest = new OrderCreateRequest(
                DELIVERY,
                PaymentMethod.CASH,
                "Good",
                1L,
                1L,
                List.of(new OrderItemRequest(
                        1L,
                        2,
                        List.of())
                )
        );
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(storeRepository.incrementAndGetOrderNumber(1L)).thenReturn(List.of(1L));
        when(productRepository.decreaseStockAndGet(1L, 2)).thenReturn(List.of(98));
        when(storeDeliverySettingRepository.findByStoreId(1L)).thenReturn(Optional.of(storeDeliverySetting));
        when(storeDeliveryAreaRepository.findByDistrictNameAndStoreId(customerAddress.getCity(), 1L)).thenReturn(Optional.of(storeDeliveryArea));

        OrderDTO oneLineResult = orderService.createOrder(1L, oneLinesRequest);

        when(productRepository.decreaseStockAndGet(1L, 1)).thenReturn(List.of(99));

        OrderCreateRequest twoLinesRequest = new OrderCreateRequest(
                DELIVERY,
                PaymentMethod.CASH,
                "Good",
                1L,
                1L,
                List.of(
                        new OrderItemRequest(1L, 1, List.of()),
                        new OrderItemRequest(1L, 1, List.of())
                )
        );
        OrderDTO twoLinesResult = orderService.createOrder(1L, twoLinesRequest);

        assertEquals(new BigDecimal("0.02"), oneLineResult.discountTotal());
        assertEquals(oneLineResult.discountTotal(), twoLinesResult.discountTotal());
    }
}