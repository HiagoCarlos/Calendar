package br.com.calendar.user;

import br.com.calendar.configuration.ConfigurationService;
import br.com.calendar.user.dto.ChangePasswordDTO;
import br.com.calendar.user.dto.ConfirmEmailDTO;
import br.com.calendar.user.dto.CreateUserDTO;
import br.com.calendar.user.dto.OtpResponseDTO;
import br.com.calendar.user.dto.UpdateUserDTO;
import br.com.calendar.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final long OTP_VALIDITY_MINUTES = 15;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ConfigurationService configurationService;

    public UserResponseDTO createUser(CreateUserDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to complete registration");
        }

        User user = userMapper.toEntity(dto);
        user.setEmailConfirmed(false);
        user.setPassword(passwordEncoder.encode(dto.password()));

        User savedUser = userRepository.save(user);
        configurationService.createDefaultConfiguration(savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    public UserResponseDTO getUserByID(String id) {
        User user = findUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        return userMapper.toResponse(user);
    }

    public UserResponseDTO updateUser(String id, UpdateUserDTO dto) {
        User user = findUserOrThrow(id);

        if (dto.name() != null) {
            user.setName(dto.name());
        }
        if (dto.email() != null) {
            user.setEmail(dto.email());
        }

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public OtpResponseDTO generateEmailConfirmationOtp(String id) {
        User user = findUserOrThrow(id);

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        userRepository.save(user);

        // TODO: send this via email once an email service exists. Returning it
        // directly for now so the confirmation flow can be tested manually.
        return new OtpResponseDTO(otp);
    }

    public UserResponseDTO confirmEmail(String id, ConfirmEmailDTO dto) {
        User user = findUserOrThrow(id);

        if (user.getOtp() == null || user.getOtpExpiration() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No confirmation code was requested");
        }

        if (user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmation code expired");
        }

        if (!user.getOtp().equals(dto.otp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid confirmation code");
        }

        user.setEmailConfirmed(true);
        user.setOtp(null);
        user.setOtpExpiration(null);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public void changePassword(String id, ChangePasswordDTO dto) {
        User user = findUserOrThrow(id);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1_000_000);
        return String.format("%06d", otp);
    }
}