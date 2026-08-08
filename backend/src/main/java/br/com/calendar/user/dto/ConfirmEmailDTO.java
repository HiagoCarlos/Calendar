package br.com.calendar.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailDTO(
        @NotBlank(message = "Confirmation code is required")
        String otp) {
}
