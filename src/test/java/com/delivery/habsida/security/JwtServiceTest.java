package com.delivery.habsida.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "unit-test-secret-key-with-at-least-32-bytes");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    public void shouldGenerateToken() {
        String token = jwtService.generateToken( 1L,"test@test.com", List.of("ADMIN"));
        System.out.println("token: " + token);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void extractEmailShouldReturnOriginalEmail() {
        String token2 = jwtService.generateToken(2L,"test2@test.com", List.of("ADMIN"));
        String email = jwtService.extractEmail(token2);
        assertEquals("test2@test.com", email);
    }
}