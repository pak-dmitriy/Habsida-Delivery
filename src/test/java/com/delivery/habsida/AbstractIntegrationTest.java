package com.delivery.habsida;

import com.delivery.habsida.entity.*;
import com.delivery.habsida.repository.*;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=test-secret-key-for-integration-tests-min-32-bytes")

public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    static {
        postgres.start();
    }

    protected static final String PASSWORD = "123456";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected UserRoleRepository userRoleRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected StoreRepository storeRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected CustomerRepository customerRepository;
    @Autowired
    protected UserStoreAccessRepository userStoreAccessRepository;
    @Autowired
    private CustomerAddressRepository customerAddressRepository;
    @Autowired
    protected StoreDeliverySettingRepository storeDeliverySettingRepository;
    @Autowired
    protected StoreDeliveryAreaRepository storeDeliveryAreaRepository;

    protected void createDeliverySetup(Store store) {
        StoreDeliverySetting setting = new StoreDeliverySetting();
        setting.setStore(store);
        setting.setDeliveryEnabled(true);
        setting.setDeliveryType(DeliveryType.SAME_DAY);
        setting.setMinOrderPrice(BigDecimal.ZERO);
        setting.setFreeDeliveryThreshold(BigDecimal.ZERO);
        setting.setMaxDistance(new BigDecimal("10"));
        setting.setDeliveryGeography(DeliveryGeography.ONLY_IN_THE_CITY);
        storeDeliverySettingRepository.save(setting);

        StoreDeliveryArea area = new StoreDeliveryArea();
        area.setStore(store);
        area.setDistrictName("City");
        area.setDeliveryFee(new BigDecimal("5.00"));
        storeDeliveryAreaRepository.save(area);
    }

    protected User createUser(String email, String roleName) {
        User user = new User();
        user.setUserName(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("+1000000");
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(roleRepository.findByName(roleName).orElseThrow());
        userRoleRepository.save(userRole);
        return user;
    }

    protected String loginAndGetToken(String email) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected Store createStore(String slug) {
        Store store = new Store();
        store.setName(slug);
        store.setStoreSlug(slug);
        store.setDescription("Test store");
        store.setTypeStoreServices(TypeStoreServices.RESTAURANT);
        store.setPhone("+1000000");
        store.setLogo("Test logo");
        store.setPickupAddress("street 1");
        store.setStatus(Status.ACTIVE);
        store.setDeliveryFee(new BigDecimal("5.00"));
        Store saved = storeRepository.save(store);
        createDeliverySetup(saved);
        return saved;
    }

    protected void giveAccess(User user, Store store) {
        UserStoreAccess userStoreAccess = new UserStoreAccess();
        userStoreAccess.setUser(user);
        userStoreAccess.setStore(store);
        userStoreAccessRepository.save(userStoreAccess);
    }

    protected Product createProduct(Store store, String name, String price) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test category");
        category.setCategoryPhoto("Photo.png");
        category.setStore(store);
        categoryRepository.save(category);

        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(new BigDecimal(price));
        product.setStock(100);
        product.setLowStockThreshold(5);
        product.setStatus(ProductStatus.AVAILABLE);
        product.setDiscountPercent(0);
        product.setMinQuantity(1);
        product.setMaxQuantity(10);
        product.setStore(store);
        product.setCategory(category);
        return productRepository.save(product);
    }

   protected CustomerAddress createCustomer(String phone) {
        Customer customer = new Customer();
        customer.setName(phone);
        customer.setPhone(phone);
        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(customer);

        CustomerAddress address = new CustomerAddress();
        address.setCity("City");
        address.setStreet("Street");
        address.setHouse("House");
        address.setApartment("Apartment");
        address.setCustomer(customer);
        return customerAddressRepository.save(address);
   }
}