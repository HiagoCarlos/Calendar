package br.com.calendar.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}
