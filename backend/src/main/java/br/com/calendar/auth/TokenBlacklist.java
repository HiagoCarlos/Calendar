package br.com.calendar.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;

    public TokenBlacklist(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public void revoke(String token) {
        Date expiration = jwtUtil.getExpirationDate(token);
        revokedTokens.put(token, expiration.toInstant());
    }

    public boolean isRevoked(String token) {
        return revokedTokens.containsKey(token);
    }

    public void cleanExpired() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}