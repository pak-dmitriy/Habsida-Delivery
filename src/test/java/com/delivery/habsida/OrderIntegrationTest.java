package com.delivery.habsida;

import com.delivery.habsida.entity.CustomerAddress;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderIntegrationTest extends AbstractIntegrationTest {

    private Long createOrderAndGetId(Store store, Product product, CustomerAddress address, String token) throws Exception {
        String body = """
                {
                "type": "DELIVERY",
                "method": "CASH",
                "customerId": %d,
                "customerAddressId": %d,
                "items": [ { "productId": %d, "quantity": 1 } ]
                }
                """.formatted(address.getCustomer().getId(), address.getId(), product.getId());

        String response = mockMvc.perform(post("/stores/" + store.getId() + "/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    void createOrder_shouldReturn201AndCalculateTotal() throws Exception {
        User merchant = createUser("order-merchant@test.com", "MERCHANT");
        Store store = createStore("order-store");
        giveAccess(merchant, store);
        Product product = createProduct(store, "Pizza", "10.00");
        CustomerAddress customerAddress = createCustomer("+1000555");
        String token = loginAndGetToken("order-merchant@test.com");

        String body = """
                {
                "type": "DELIVERY",
                "method": "CASH",
                "customerId": %d,
                "customerAddressId": %d,
                "items": [ { "productId": %d, "quantity": 2 } ]
                }
                """
                .formatted(customerAddress.getCustomer().getId(), customerAddress.getId(), product.getId());

        mockMvc.perform(post("/stores/" + store.getId() + "/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus").value("CREATED"))
                .andExpect(jsonPath("$.total").value(25.0));
    }

    @Test
    void createOrder_shouldReturn403_whenMerchantHasNoAccessToStore() throws Exception {
        createUser("order-merchant1@test.com", "MERCHANT");
        Store store = createStore("order-store1");
        Product product = createProduct(store, "Pizza1", "10.00");
        CustomerAddress customerAddress = createCustomer("+1000556");
        String token = loginAndGetToken("order-merchant1@test.com");

        String body = """
                {
                "type": "DELIVERY",
                "method": "CASH",
                "customerId": %d,
                "customerAddressId": %d,
                "items": [ { "productId": %d, "quantity": 2 } ]
                }
                """
                .formatted(customerAddress.getCustomer().getId(), customerAddress.getId(), product.getId());

        mockMvc.perform(post("/stores/" + store.getId() + "/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void orderLifecycle_shouldGoFromCreatedToCompleted() throws Exception {
        User merchant = createUser("order-merchant2@test.com", "MERCHANT");
        Store store = createStore("order-store2");
        giveAccess(merchant, store);
        Product product = createProduct(store, "Pizza2", "10.00");
        CustomerAddress customerAddress = createCustomer("+1000552");
        String token = loginAndGetToken("order-merchant2@test.com");

        Long orderId = createOrderAndGetId(store, product, customerAddress, token);

        mockMvc.perform(post("/stores/" + store.getId() + "/orders/" + orderId + "/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("ACCEPTED"));

        mockMvc.perform(post("/stores/" + store.getId() + "/orders/" + orderId + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("IN_PROGRESS"));

        mockMvc.perform(post("/stores/" + store.getId() + "/orders/" + orderId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("COMPLETED"));

    }

    @Test
    void completeOrder_shouldReturn400_whenOrderIsOnlyCreated() throws Exception {
        User merchant = createUser("order-merchant3@test.com", "MERCHANT");
        Store store = createStore("order-store3");
        giveAccess(merchant, store);
        Product product = createProduct(store, "Pizza3", "10.00");
        CustomerAddress customerAddress = createCustomer("+1000553");
        String token = loginAndGetToken("order-merchant3@test.com");

        Long orderId = createOrderAndGetId(store, product, customerAddress, token);

        mockMvc.perform(post("/stores/" + store.getId() + "/orders/" + orderId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));    }
}
