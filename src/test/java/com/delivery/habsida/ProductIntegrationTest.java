package com.delivery.habsida;

import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createProduct_shouldReturn201_whenMerchantHasAccessToStore() throws Exception {
        User merchant = createUser("product-merchant@test.com", "MERCHANT");
        Store store = createStore("product-store");
        giveAccess(merchant, store);
        Product existing = createProduct(store, "Seed", "1.00");
        String token = loginAndGetToken("product-merchant@test.com");

        String body = """
                {
                  "name": "Burger",
                  "description": "Tasty burger",
                  "price": 7.50,
                  "stock": 20,
                  "lowStockThreshold": 3,
                  "status": "AVAILABLE",
                  "discountPercent": 0,
                  "maxQuantity": 5,
                  "minQuantity": 1,
                  "categoryId": %d
                }
                """.formatted(existing.getCategory().getId());

        mockMvc.perform(post("/stores/" + store.getId() + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Burger"))
                .andExpect(jsonPath("$.storeId").value(store.getId()));
    }

    @Test
    void createProduct_shouldReturn403_whenMerchantHasNoAccessToStore() throws Exception {
        createUser("product-merchant5@test.com", "MERCHANT");
        Store store = createStore("product-store5");
        Product existing = createProduct(store, "Seed5", "1.00");
        String token = loginAndGetToken("product-merchant5@test.com");

        String body = """
                {
                  "name": "Burger",
                  "description": "Tasty burger",
                  "price": 7.50,
                  "stock": 20,
                  "lowStockThreshold": 3,
                  "status": "AVAILABLE",
                  "discountPercent": 0,
                  "maxQuantity": 5,
                  "minQuantity": 1,
                  "categoryId": %d
                }
                """.formatted(existing.getCategory().getId());

        mockMvc.perform(post("/stores/" + store.getId() + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

    }
}