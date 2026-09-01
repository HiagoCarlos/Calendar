package br.com.calendar.user;

import br.com.calendar.common.exception.GlobalExceptionHandler;
import br.com.calendar.common.exception.ResourceNotFoundException;
import br.com.calendar.user.dto.ChangePasswordDTO;
import br.com.calendar.user.dto.UpdateUserDTO;
import br.com.calendar.user.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String USER_ID = "usr_" + UUID.randomUUID();

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMe_Returns200_WhenValidToken() throws Exception {
        UserResponseDTO response = new UserResponseDTO(USER_ID, "Test User", "test@example.com", true, null, null);
        when(userService.getUserByID(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getMe_Returns404_WhenUserNotFound() throws Exception {
        when(userService.getUserByID(USER_ID)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void updateUser_Returns200_WhenValid() throws Exception {
        UserResponseDTO response = new UserResponseDTO(USER_ID, "Updated User", "test@example.com", true, null, null);
        when(userService.updateUser(eq(USER_ID), any(UpdateUserDTO.class))).thenReturn(response);

        String payload = """
                {
                    "name": "Updated User"
                }
                """;

        mockMvc.perform(patch("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"));
    }

    @Test
    void updateUser_Returns400_WhenEmailIsInvalid() throws Exception {
        String payload = """
                {
                    "email": "invalid-email"
                }
                """;

        mockMvc.perform(patch("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid fields"))
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void changePassword_Returns204_WhenValid() throws Exception {
        doNothing().when(userService).changePassword(eq(USER_ID), any(ChangePasswordDTO.class));

        String payload = """
                {
                    "currentPassword": "oldPassword123",
                    "newPassword": "newPassword123"
                }
                """;

        mockMvc.perform(patch("/users/me/password")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_Returns400_WhenFieldsMissing() throws Exception {
        String payload = """
                {
                    "newPassword": "newPassword123"
                }
                """;

        mockMvc.perform(patch("/users/me/password")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid fields"))
                .andExpect(jsonPath("$.fields.currentPassword").exists());
    }
}
