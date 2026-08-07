package br.com.calendar.auth;

import br.com.calendar.auth.dto.AuthResponse;
import br.com.calendar.auth.dto.LoginRequest;
import br.com.calendar.auth.dto.SignupRequest;
import br.com.calendar.user.User;
import br.com.calendar.user.UserRepository;
import br.com.calendar.user.UserService;
import br.com.calendar.user.dto.CreateUserDTO;
import br.com.calendar.user.dto.UserResponseDTO;
import br.com.calendar.user.dto.UserSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USER_ID = "usr_abc123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklist tokenBlacklist;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userService, passwordEncoder, jwtUtil, tokenBlacklist);
    }

    @Test
    void signupCreatesUserAndReturnsToken() {
        UserResponseDTO created = new UserResponseDTO(
                USER_ID, "Danillo", "test@example.com", false, Instant.now(), Instant.now());

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(created);
        when(jwtUtil.generateToken(USER_ID)).thenReturn("jwt-token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.signup(
                new SignupRequest("Danillo", "test@example.com", "plain-password"));

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void loginWithValidCredentialsReturnsToken() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken(USER_ID)).thenReturn("jwt-token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(new LoginRequest("test@example.com", "plain-password"));

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void loginWithWrongPasswordThrows() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("test@example.com", "wrong-password")));
    }

    @Test
    void loginWithUnknownEmailThrows() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("unknown@example.com", "any-password")));
    }

    @Test
    void meReturnsUserData() {
        User user = new User();
        user.setId(USER_ID);
        user.setName("Danillo");
        user.setEmail("test@example.com");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserSummaryDTO response = authService.me(USER_ID);

        assertEquals(USER_ID, response.id());
        assertEquals("Danillo", response.name());
        assertEquals("test@example.com", response.email());
    }

    @Test
    void logoutRevokesToken() {
        authService.logout("jwt-token");

        verify(tokenBlacklist).revoke("jwt-token");
    }

    @Test
    void logoutWithBlankTokenThrows() {
        assertThrows(ResponseStatusException.class, () -> authService.logout(" "));
    }
}