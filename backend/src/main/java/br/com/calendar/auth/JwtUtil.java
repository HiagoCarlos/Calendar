package br.com.calendar.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    public static final String SCOPE_PASSWORD_RESET = "password_reset";

    private static final String SCOPE_CLAIM = "scope";

    private final SecretKey key;
    @Getter
    private final long expirationMs;
    @Getter
    private final long resetExpirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs,
                   @Value("${jwt.reset-expiration-ms}") long resetExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.resetExpirationMs = resetExpirationMs;
    }

    public String generateToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String generatePasswordResetToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim(SCOPE_CLAIM, SCOPE_PASSWORD_RESET)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + resetExpirationMs))
                .signWith(key)
                .compact();
    }

    public String getScope(String token) {
        return parseClaims(token).get(SCOPE_CLAIM, String.class);
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public Date getExpirationDate(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}