package com.example.hospital_manager.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken("PS/2022/001", "PATIENT");
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("PS/2022/001", username);

        String role = jwtService.extractRole(token);
        assertEquals("PATIENT", role);

        UserDetails userDetails = new User("PS/2022/001", "pass", Collections.emptyList());
        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void testTamperedTokenFailsValidation() {
        String token = jwtService.generateToken("PS/2022/001", "PATIENT");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        String originalPayload = new String(Base64.getUrlDecoder().decode(parts[1]));
        String tamperedPayload = originalPayload.replace("\"PATIENT\"", "\"ADMIN\"");
        String encodedTamperedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(tamperedPayload.getBytes());

        String tamperedToken = parts[0] + "." + encodedTamperedPayload + "." + parts[2];

        assertThrows(JwtException.class, () -> {
            jwtService.extractAllClaims(tamperedToken);
        });

        UserDetails userDetails = new User("PS/2022/001", "pass", Collections.emptyList());
        assertFalse(jwtService.validateToken(tamperedToken, userDetails));
    }
}
