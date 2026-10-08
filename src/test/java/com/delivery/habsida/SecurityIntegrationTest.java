package com.delivery.habsida;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityIntegrationTest extends  AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void protectedEndpoint_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/stores/1/orders/new"))
                .andExpect(status().isUnauthorized());
    }
}
