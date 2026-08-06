package br.com.calendar.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "dev-only-secret-change-in-production-0123456789abcdef";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 86400000L);
    }

    @Test
    void generateTokenAndExtractEmail() {
        String token = jwtUtil.generateToken("test@example.com");

        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void tokenIsNotExpired() {
        String token = jwtUtil.generateToken("test@example.com");

        assertFalse(jwtUtil.isExpired(token));
    }

    @Test
    void expiredTokenIsDetected() {
        JwtUtil shortLivedJwt = new JwtUtil(SECRET, -1000L);
        String token = shortLivedJwt.generateToken("test@example.com");

        assertTrue(shortLivedJwt.isExpired(token));
    }

    @Test
    void invalidTokenThrows() {
        assertThrows(Exception.class, () -> jwtUtil.extractEmail("invalid.token.here"));
    }
}
