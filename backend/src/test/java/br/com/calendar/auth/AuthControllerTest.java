package br.com.calendar.auth;

import br.com.calendar.auth.dto.AuthResponse;
import br.com.calendar.auth.dto.ConfirmEmailRequest;
import br.com.calendar.auth.dto.ForgotPasswordRequest;
import br.com.calendar.auth.dto.LoginRequest;
import br.com.calendar.auth.dto.ResetPasswordRequest;
import br.com.calendar.auth.dto.SignupRequest;
import br.com.calendar.auth.dto.VerifyOtpRequest;
import br.com.calendar.auth.dto.VerifyOtpResponse;
import br.com.calendar.user.UserService;
import br.com.calendar.user.dto.OtpResponseDTO;
import br.com.calendar.user.dto.UserResponseDTO;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String NAME = "Test User";
    private static final String EMAIL = "test-auth@example.com";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // signup creates user if not exists
        try {
            authService.signup(new SignupRequest(NAME, EMAIL, PASSWORD));
        } catch (Exception ignored) {
            // user already exists
        }
    }

    @Test
    void signupReturns201WithToken() throws Exception {
        SignupRequest request = new SignupRequest("New User", "new-" + System.currentTimeMillis() + "@example.com", PASSWORD);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginReturns200WithToken() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }



    @Test
    void logoutReturns204() throws Exception {
        // login to get a token
        LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        String token = authResponse.accessToken();

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutWithRevokedTokenReturns401() throws Exception {
        // login and logout to revoke the token
        LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        String token = authResponse.accessToken();

        // first logout
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // try to use the revoked token
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPasswordWithValidResetTokenSucceedsAndChangesThePassword() throws Exception {
        String email = "reset-" + System.currentTimeMillis() + "@example.com";
        authService.signup(new SignupRequest("Reset User", email, PASSWORD));
        UserResponseDTO user = userService.getUserByEmail(email);
        OtpResponseDTO otp = userService.generatePasswordResetOtp(user.id());

        VerifyOtpResponse verifyOtpResponse = verifyOtp(email, otp.otp());

        ResetPasswordRequest request = new ResetPasswordRequest(
                verifyOtpResponse.resetToken(), "newPassword123", "newPassword123");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "newPassword123"))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPasswordWithAlreadyUsedResetTokenReturns400() throws Exception {
        String email = "reset-reuse-" + System.currentTimeMillis() + "@example.com";
        authService.signup(new SignupRequest("Reset User", email, PASSWORD));
        UserResponseDTO user = userService.getUserByEmail(email);
        OtpResponseDTO otp = userService.generatePasswordResetOtp(user.id());

        VerifyOtpResponse verifyOtpResponse = verifyOtp(email, otp.otp());
        ResetPasswordRequest request = new ResetPasswordRequest(
                verifyOtpResponse.resetToken(), "newPassword123", "newPassword123");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordWithAccessTokenInsteadOfResetTokenReturns400() throws Exception {
        LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);

        ResetPasswordRequest request = new ResetPasswordRequest(
                authResponse.accessToken(), "newPassword123", "newPassword123");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordWithMismatchedPasswordsReturns400() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("some-token", "newPassword123", "differentPassword");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordWithBlankResetTokenReturns400() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("", "newPassword123", "newPassword123");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmEmailWithValidTokenMarksEmailConfirmed() throws Exception {
        String email = "confirm-" + System.currentTimeMillis() + "@example.com";
        authService.signup(new SignupRequest("Confirm User", email, PASSWORD));
        UserResponseDTO user = userService.getUserByEmail(email);
        assertFalse(user.emailConfirmed());

        String token = jwtUtil.generateEmailConfirmationToken(user.id());

        mockMvc.perform(post("/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmEmailRequest(token))))
                .andExpect(status().isOk());

        assertTrue(userService.getUserByEmail(email).emailConfirmed());
    }

    @Test
    void confirmEmailWithAccessTokenInsteadOfConfirmationTokenReturns400() throws Exception {
        LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);

        mockMvc.perform(post("/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmEmailRequest(authResponse.accessToken()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmEmailWithBlankTokenReturns400() throws Exception {
        mockMvc.perform(post("/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmEmailRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupReturns400WhenEmailAlreadyExists() throws Exception {
        SignupRequest request = new SignupRequest("Test User", EMAIL, PASSWORD);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupReturns400WhenEmailIsInvalid() throws Exception {
        SignupRequest request = new SignupRequest("New User", "invalid-email", PASSWORD);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void loginReturns401WhenEmailDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPasswordReturns200ForValidEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest(EMAIL);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void forgotPasswordReturns200ForNonexistentEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void verifyOtpReturns200WithValidOtp() throws Exception {
        String email = "otp-valid-" + System.currentTimeMillis() + "@example.com";
        authService.signup(new SignupRequest("OTP User", email, PASSWORD));
        UserResponseDTO user = userService.getUserByEmail(email);
        OtpResponseDTO otp = userService.generatePasswordResetOtp(user.id());

        VerifyOtpRequest request = new VerifyOtpRequest(email, otp.otp());

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetToken", notNullValue()));
    }

    @Test
    void verifyOtpReturns400WithInvalidOtp() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest(EMAIL, "123456");

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private VerifyOtpResponse verifyOtp(String email, String otp) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyOtpRequest(email, otp))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), VerifyOtpResponse.class);
    }
}