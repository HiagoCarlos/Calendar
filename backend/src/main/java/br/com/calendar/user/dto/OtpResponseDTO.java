package br.com.calendar.user.dto;

// Temporary: exposes the OTP directly in the response until email sending is implemented.
// Once there's an email service, this endpoint should stop returning the code and just confirm it was sent.
public record OtpResponseDTO(String otp) {
}
