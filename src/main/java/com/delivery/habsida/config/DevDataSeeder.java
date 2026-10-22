package com.delivery.habsida.config;

import com.delivery.habsida.dto.*;
import com.delivery.habsida.entity.*;
import com.delivery.habsida.repository.*;
import com.delivery.habsida.service.EmployeeService;
import com.delivery.habsida.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;


@Component
@Profile("dev")
@ConditionalOnProperty(
        prefix = "app.seed",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreRepository storeRepository;
    private final EmployeeService employeeService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final StoreDeliverySettingRepository storeDeliverySettingRepository;
    private final StoreDeliveryAreaRepository storeDeliveryAreaRepository;
    private final OrderService orderService;
    private final StoreHourRepository storeHourRepository;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.merchant-password}")
    private String merchantPassword;

    public DevDataSeeder(UserRepository userRepository,
                         RoleRepository roleRepository,
                         UserRoleRepository userRoleRepository,
                         PasswordEncoder passwordEncoder,
                         StoreRepository storeRepository,
                         EmployeeService employeeService,
                         CategoryRepository categoryRepository,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository,
                         CustomerAddressRepository customerAddressRepository,
                         StoreDeliverySettingRepository storeDeliverySettingRepository,
                         StoreDeliveryAreaRepository storeDeliveryAreaRepository,
                         OrderService orderService,
                         StoreHourRepository storeHourRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.storeRepository = storeRepository;
        this.employeeService = employeeService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.storeDeliverySettingRepository = storeDeliverySettingRepository;
        this.storeDeliveryAreaRepository = storeDeliveryAreaRepository;
        this.orderService = orderService;
        this.storeHourRepository = storeHourRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.findByEmail("admin@habsida.com").isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUserName("admin");
        admin.setEmail("admin@habsida.com");
        admin.setFirstName("Admin");
        admin.setLastName("Habsida");
        admin.setPhone("01012345678");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        User savedUser = userRepository.save(admin);
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(adminRole);
        userRoleRepository.save(userRole);

        Store burgerStore = createStore("Burger House", "burger-house", TypeStoreServices.FAST_FOOD);

        for (DayOfWeek day : DayOfWeek.values()) {
            StoreHour storeHour = new StoreHour();
            storeHour.setStore(burgerStore);
            storeHour.setDayOff(false);
            storeHour.setDayOfWeek(day);
            storeHour.setOpenTime(LocalTime.of(0, 0));
            storeHour.setCloseTime(LocalTime.of(23,59));
            storeHourRepository.save(storeHour);
        }

        StoreDeliverySetting storeDeliverySetting = new StoreDeliverySetting();
        storeDeliverySetting.setStore(burgerStore);
        storeDeliverySetting.setDeliveryEnabled(true);
        storeDeliverySetting.setDeliveryType(DeliveryType.SAME_DAY);
        storeDeliverySetting.setMinOrderPrice(new BigDecimal("5.0"));
        storeDeliverySetting.setFreeDeliveryThreshold(new BigDecimal("40.0"));
        storeDeliverySetting.setMaxDistance(new BigDecimal("10.0"));
        storeDeliverySetting.setDeliveryGeography(DeliveryGeography.ONLY_IN_THE_CITY);
        storeDeliverySettingRepository.save(storeDeliverySetting);

        StoreDeliveryArea storeDeliveryArea = new StoreDeliveryArea();
        storeDeliveryArea.setStore(burgerStore);
        storeDeliveryArea.setDistrictName("New York");
        storeDeliveryArea.setDeliveryFee(new BigDecimal("3.0"));
        storeDeliveryAreaRepository.save(storeDeliveryArea);

        EmployeeCreateResponse employee = employeeService.createEmployee(
                new EmployeeCreateRequest("merchant", "merchant@habsida.com", merchantPassword,
                        "Tony", "Stark", "01099998888", "MERCHANT"));

        employeeService.assignEmployeeToStore(employee.id(), burgerStore.getId());

        Category burgers = createCategory(burgerStore, "Burgers", "Burgers");
        Category drinks = createCategory(burgerStore, "Drinks", "Drinks");
        Category sides = createCategory(burgerStore, "Sides", "Sides");

        Product doubleCheeseBurger = createProduct(burgerStore, burgers, "Double Cheese Burger",
                "Double Cheese Burger", "10.0", 10);
        Product sprite = createProduct(burgerStore, drinks, "Sprite", "Cold Sprite",
                "2.0", 10);
        Product potato = createProduct(burgerStore, sides, "Potato", "Sweet Potato", "4.0", 20);

        Customer customer = createCustomer("Piter Parker", "01077776666");
        CustomerAddress customerAddress = createCustomerAddress(customer, "New York", "Lenina", "12", "2");

        Customer customer1 = createCustomer("Ben Parker", "01055554444");
        CustomerAddress customerAddress1 = createCustomerAddress(customer1, "New York", "Pushkina", "1", "12");

        orderService.createOrder(burgerStore.getId(),
                new OrderCreateRequest(OrderType.DELIVERY, PaymentMethod.ONLINE, "Call before arrival",
                        customer.getId(), customerAddress.getId(),
                        List.of(new OrderItemRequest(doubleCheeseBurger.getId(), 2, null))));

        orderService.createOrder(burgerStore.getId(),
                new OrderCreateRequest(OrderType.PICKUP, PaymentMethod.CASH, null,
                        customer1.getId(), customerAddress1.getId(),
                        List.of(new OrderItemRequest(sprite.getId(), 2, null),
                                new OrderItemRequest(potato.getId(), 3, null))));

    }

    private Store createStore(String name, String slug, TypeStoreServices typeStoreServices) {
        Store store = new Store();
        store.setName(name);
        store.setStoreSlug(slug);
        store.setTypeStoreServices(typeStoreServices);
        store.setDescription("test");
        store.setPhone("01012345678");
        store.setLogo("test.png");
        store.setPickupAddress("Test address");
        store.setStatus(Status.ACTIVE);
        return storeRepository.save(store);
    }

    private Category createCategory(Store store, String name, String description) {
        Category category = new Category();
        category.setStore(store);
        category.setName(name);
        category.setDescription(description);
        category.setCategoryPhoto("test.png");
        return categoryRepository.save(category);
    }

    private Product createProduct(Store store, Category category, String name, String description, String price, int stock) {
        Product product = new Product();
        product.setStore(store);
        product.setCategory(category);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setStatus(ProductStatus.AVAILABLE);
        product.setLowStockThreshold(2);
        product.setDiscountPercent(0);
        product.setMinQuantity(1);
        product.setMaxQuantity(10);
        return productRepository.save(product);
    }

    private Customer createCustomer(String name, String phone) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setStatus(CustomerStatus.ACTIVE);
        return customerRepository.save(customer);
    }

    private CustomerAddress createCustomerAddress(Customer customer, String city, String street, String house, String apartment) {
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setCustomer(customer);
        customerAddress.setCity(city);
        customerAddress.setStreet(street);
        customerAddress.setHouse(house);
        customerAddress.setApartment(apartment);
        return customerAddressRepository.save(customerAddress);
    }
}
