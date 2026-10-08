package com.delivery.habsida;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void login_shouldReturnToken_whenCredentialAreValid() throws Exception {
        createUser("admin1@test.com", "ADMIN");

        String token = loginAndGetToken("admin1@test.com");

        assertFalse(token.isBlank());
    }
}