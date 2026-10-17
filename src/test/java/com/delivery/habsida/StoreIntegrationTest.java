package com.delivery.habsida;

import com.delivery.habsida.entity.Store;
import com.delivery.habsida.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void getStores_shouldReturnOnlyOwnStores_whenUserIsMerchant() throws Exception {
        User merchant = createUser("stores-merchant@test.com", "MERCHANT");
        Store ownStore = createStore("own-store");
        createStore("foreign-store");
        giveAccess(merchant, ownStore);
        String token = loginAndGetToken("stores-merchant@test.com");

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].storeSlug").value("own-store"));
    }

    @Test
    void getStores_shouldReturnAllStores_whenUserIsAdmin() throws Exception {
        createUser("stores-admin@test.com", "ADMIN");
        createStore("admin-store-1");
        createStore("admin-store-2");
        String token = loginAndGetToken("stores-admin@test.com");

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].storeSlug", hasItems("admin-store-1", "admin-store-2")));
    }

    @Test
    void getStores_shouldReturnSortedByNameAsc_whenUserIsMerchant() throws Exception {
        User merchant = createUser("merchant@merchant", "MERCHANT");

        Store bStore = createStore("store-b");
        Store aStore = createStore("store-a");
        createStore("store-c");

        giveAccess(merchant, bStore);
        giveAccess(merchant, aStore);

        String token = loginAndGetToken("merchant@merchant");

        mockMvc.perform(get("/api/stores")
                        .param("sort", "name,asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].storeSlug").value("store-a"))
                .andExpect(jsonPath("$.content[1].storeSlug").value("store-b"))
                .andExpect(jsonPath("$.content[2].storeSlug").doesNotExist());
    }

    @Test
    void getStores_shouldReturnSortedByNameDesc_whenUserIsMerchant() throws Exception {
        User merchant = createUser("mer@mer", "MERCHANT");

        Store storeB = createStore("store-desc-b");
        Store storeA = createStore("store-desc-a");
        createStore("store-desc-c");

        giveAccess(merchant, storeA);
        giveAccess(merchant, storeB);

        String token = loginAndGetToken("mer@mer");

        mockMvc.perform(get("/api/stores")
                        .param("sort", "name,desc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].storeSlug").value("store-desc-b"))
                .andExpect(jsonPath("$.content[1].storeSlug").value("store-desc-a"))
                .andExpect(jsonPath("$.content[2].storeSlug").doesNotExist());
    }

    @Test
    void getStores_shouldReturnSortedByNameAsc_whenUserIsAdmin() throws Exception {
        createUser("admin@admin", "ADMIN");

        createStore("aaa-store-admin-b");
        createStore("aaa-store-admin-a");

        String token = loginAndGetToken("admin@admin");

        String body = mockMvc.perform(get("/api/stores")
                        .param("sort", "name,asc")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> names = JsonPath.read(body, "$.content[*].name");
        int indexA = names.indexOf("aaa-store-admin-a");
        int indexB = names.indexOf("aaa-store-admin-b");

        assertNotEquals(-1, indexA);
        assertNotEquals(-1, indexB);
        assertTrue(indexA < indexB);
    }

    @Test
    void getStores_shouldReturnSortedByNameDesc_whenUserIsAdmin() throws Exception {
        createUser("admin1@admin1", "ADMIN");

        createStore("zzz-store-admin-a");
        createStore("zzz-store-admin-b");

        String token = loginAndGetToken("admin1@admin1");

        String body = mockMvc.perform(get("/api/stores")
                        .param("sort", "name,desc")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> names = JsonPath.read(body, "$.content[*].name");
        int indexB = names.indexOf("zzz-store-admin-b");
        int indexA = names.indexOf("zzz-store-admin-a");

        assertNotEquals(-1, indexA);
        assertNotEquals(-1, indexB);
        assertTrue(indexA > indexB);
    }

    @Test
    void getStores_shouldReturnBadRequest_whenSortFieldIsInvalid() throws Exception {
        createUser("ad@ad", "ADMIN");
        String token = loginAndGetToken("ad@ad");

        mockMvc.perform(get("/api/stores")
                        .param("sort", "foo,asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
