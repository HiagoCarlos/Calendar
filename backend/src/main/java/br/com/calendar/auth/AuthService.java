package br.com.calendar.auth;

import br.com.calendar.auth.dto.AuthResponse;
import br.com.calendar.auth.dto.ForgotPasswordRequest;
import br.com.calendar.auth.dto.LoginRequest;
import br.com.calendar.auth.dto.SignupRequest;
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

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, UserService userService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

    public UserSummaryDTO me(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserSummaryDTO(user.getId(), user.getName(), user.getEmail());
    }
}