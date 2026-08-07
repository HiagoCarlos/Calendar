package br.com.calendar.auth;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
public class TokenBlacklist {

    // WARNING: isso é in-memory. Se rodar mais de uma instância do backend
    // (load balancer, etc.), um logout numa instância não invalida o token
    // nas outras. Trocar por Redis/DB quando precisar de multi-instância.
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

    // roda a cada hora pra limpar tokens expirados da memória,
    // sem depender de alguém fazer logout pra triggerar a limpeza.
    @Scheduled(fixedRate = 3600000)
    public void cleanExpired() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}