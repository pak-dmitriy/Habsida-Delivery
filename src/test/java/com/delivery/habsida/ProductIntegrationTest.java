package com.delivery.habsida;

import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIntegrationTest extends AbstractIntegrationTest {
    private ResultActions postProduct(Long storeId, String token, String name, Long categoryId) throws Exception {
        String body = """
                {
                 "name": "%s",
                          "description": "Burger Artyom",
                          "price": 10,
                          "stock": 30,
                          "lowStockThreshold": 5,
                          "status": "AVAILABLE",
                          "discountPercent": 0,
                          "maxQuantity": 5,
                          "minQuantity": 1,
                          "categoryId": %d
                }
                """.formatted(name, categoryId);

        return mockMvc.perform(post("/stores/" + storeId + "/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

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

    @Test
    void createProduct_shouldReturn201_whenSameNameInDifferentStores() throws Exception {
        User merchantArtyom = createUser("artyom@artyom", "MERCHANT");

        Store storeBulgakov = createStore("store-bulgakov");
        Store storeBulgakov2 = createStore("store-bulgakov2");
        giveAccess(merchantArtyom, storeBulgakov);
        giveAccess(merchantArtyom, storeBulgakov2);

        String token = loginAndGetToken("artyom@artyom");

        Product artyomProduct = createProduct(storeBulgakov, "vegetables", "10");
        Product artyomProduct1 = createProduct(storeBulgakov2, "vegetables1", "10");
        Long categoryA = artyomProduct.getCategory().getId();
        Long categoryB = artyomProduct1.getCategory().getId();

        postProduct(storeBulgakov.getId(), token, "SoupArtyom", categoryA)
                .andExpect(status().isCreated());

        postProduct(storeBulgakov2.getId(), token, "SoupArtyom", categoryB)
                .andExpect(status().isCreated());
    }

    @Test
    void createProduct_shouldReturn409_whenSameNameInOneStore() throws Exception {
        User merchantPushkin = createUser("pushkin@pushkin", "MERCHANT");
        Store storePushkin = createStore("store-pushkin");
        giveAccess(merchantPushkin, storePushkin);

        String token = loginAndGetToken("pushkin@pushkin");

        Product pushkinProduct = createProduct(storePushkin, "fruits", "10");
        Long categoryC = pushkinProduct.getCategory().getId();

        postProduct(storePushkin.getId(), token, "SoupArtyom", categoryC)
                .andExpect(status().isCreated());

        postProduct(storePushkin.getId(), token, "SoupArtyom", categoryC)
                .andExpect(status().isConflict());
    }
}