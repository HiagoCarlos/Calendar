package br.com.calendar.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotBlank(message = "OTP is required")
        String otp,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Password confirmation is required")
        @Size(min = 6, message = "Password confirmation must be at least 6 characters")
        String passwordConfirmation
) {
}
