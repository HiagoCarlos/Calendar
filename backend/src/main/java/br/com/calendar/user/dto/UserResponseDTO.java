package br.com.calendar.user.dto;

import java.time.Instant;

public record UserResponseDTO(

    String id,

    String name,

    String email,

    boolean emailConfirmed,

    Instant createdAt,

    Instant updatedAt
){}
