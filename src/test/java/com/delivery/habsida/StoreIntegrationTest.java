package com.delivery.habsida;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StoreIntegrationTest extends AbstractIntegrationTest {
    private static final String STORE_JSON = """
    {
            "name":"Pizza Place",
            "storeSlug":"pizza-place",
            "description":"Best pizza",
            "typeStoreServices":"RESTAURANT",
            "phone":"+1000111",
            "logo":"logo.png",
            "pickupAddress":"Main st 1",
            "status":"ACTIVE",
            "deliveryFee":5.00
    }
    """;

    @Test
    void createStore_shouldReturn201_whenUserIsAdmin() throws Exception {
        createUser("store-admin@test.com", "ADMIN");
        String token = loginAndGetToken("store-admin@test.com");

        mockMvc.perform(post("/api/stores")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(STORE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pizza Place"));
    }

    @Test
    void createStore_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        createUser("store-merchant@test.com", "MERCHANT");
        String token = loginAndGetToken("store-merchant@test.com");

        mockMvc.perform(post("/api/stores")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
                .content(STORE_JSON))
                .andExpect(status().isForbidden());
    }
}
