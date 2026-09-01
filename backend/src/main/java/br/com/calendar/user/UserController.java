package br.com.calendar.user;

import br.com.calendar.user.dto.ChangePasswordDTO;
import br.com.calendar.user.dto.UpdateUserDTO;
import br.com.calendar.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(Principal principal) {
        return ResponseEntity.ok(userService.getUserByID(principal.getName()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUser(
            Principal principal, @Valid @RequestBody UpdateUserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(principal.getName(), dto));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(Principal principal, @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(principal.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
