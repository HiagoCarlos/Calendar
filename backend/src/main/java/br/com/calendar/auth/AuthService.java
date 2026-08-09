package br.com.calendar.auth;

import br.com.calendar.auth.dto.AuthResponse;
import br.com.calendar.auth.dto.ForgotPasswordRequest;
import br.com.calendar.auth.dto.LoginRequest;
import br.com.calendar.auth.dto.SignupRequest;
import br.com.calendar.auth.dto.VerifyOtpRequest;
import br.com.calendar.auth.dto.VerifyOtpResponse;
import br.com.calendar.common.dto.MessageResponse;
import br.com.calendar.user.User;
import br.com.calendar.user.UserRepository;
import br.com.calendar.user.UserService;
import br.com.calendar.user.dto.CreateUserDTO;
import br.com.calendar.user.dto.OtpResponseDTO;
import br.com.calendar.user.dto.UserResponseDTO;
import br.com.calendar.user.dto.UserSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(UserRepository userRepository, UserService userService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    public AuthResponse signup(SignupRequest request) {
        CreateUserDTO dto = new CreateUserDTO(request.name(), request.email(), request.password());
        UserResponseDTO created = userService.createUser(dto);
        String token = jwtUtil.generateToken(created.id());
        return new AuthResponse(token, "Bearer", jwtUtil.getExpirationMs() / 1000);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, "Bearer", jwtUtil.getExpirationMs() / 1000);
    }

    public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(
                        user -> {
                            OtpResponseDTO otpResponseDTO = userService.generateEmailConfirmationOtp(user.getId());

                            // Email added to the log message for identification during the test.
                            log.info("Generated OTP for email: {} - Code: {}", user.getEmail(), otpResponseDTO.otp());
                }
        );

        return new MessageResponse("If the email is registered, password reset instructions will be sent.");
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(AuthService::invalidOtp);

        if (!isOtpValid(user, request.otp())) {
            throw invalidOtp();
        }
        user.setOtp(null);
        user.setOtpExpiration(null);
        userRepository.save(user);

        return new VerifyOtpResponse(
                jwtUtil.generatePasswordResetToken(user.getId()),
                jwtUtil.getResetExpirationMs() / 1000);
    }

    private static boolean isOtpValid(User user, String otp) {
        return user.getOtp() != null
                && user.getOtpExpiration() != null
                && user.getOtpExpiration().isAfter(LocalDateTime.now())
                && MessageDigest.isEqual(user.getOtp().getBytes(StandardCharsets.UTF_8),
                                         otp.getBytes(StandardCharsets.UTF_8));
    }

    private static ResponseStatusException invalidOtp() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
    }

    public UserSummaryDTO me(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserSummaryDTO(user.getId(), user.getName(), user.getEmail());
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }

        // valida se é um JWT de verdade antes de colocar na blacklist.
        // se for uma string qualquer, o jwtUtil lança exceção → 400 em vez de 500.
        try {
            jwtUtil.getExpirationDate(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }

        tokenBlacklist.cleanExpired();
        tokenBlacklist.revoke(token);
    }
}