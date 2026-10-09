package com.delivery.habsida.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void encodePassword() {
        String encoded = passwordEncoder.encode("root");
        System.out.println("Encoded password: " + encoded);

        assertTrue(passwordEncoder.matches("root", encoded));
    }
}