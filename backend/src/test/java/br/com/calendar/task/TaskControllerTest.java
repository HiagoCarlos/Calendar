package br.com.calendar.task;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import br.com.calendar.common.exception.GlobalExceptionHandler;
import br.com.calendar.task.dto.TaskResponseDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same pattern as CategoryControllerTest: MockMvc hits the *real* route string
// declared in @GetMapping, so a routing mistake (e.g. an accidental
// "/tasks/history" on a method inside a class already @RequestMapping("/tasks"),
// which becomes "/tasks/tasks/history") shows up here as a 404 instead of only
// being caught by a human reading the code.
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private static final String USER_ID = "usr_abc123";

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    mockMvc = MockMvcBuilders
            .standaloneSetup(new TaskController(taskService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
}

    @Test
    void returnsThePaginatedHistoryForTheAuthenticatedUser() throws Exception {
        TaskResponseDTO task = TaskResponseDTO.builder()
                .id("tsk_123")
                .title("Old task")
                .completedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        // taskService.getTaskHistory(...) is mocked here, so this test doesn't
        // care whether the filtering/ordering logic itself is correct — that's
        // TaskServiceTest's job. This test only cares that a GET to the real
        // "/tasks/history" URL reaches the controller and returns what the
        // service gave it, in the shape the client actually receives.
        when(taskService.getTaskHistory(any()))
                .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/tasks/history")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("tsk_123"))
                .andExpect(jsonPath("$.content[0].title").value("Old task"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(taskService).getTaskHistory(any());
    }

    @Test
    void respectsCustomPageAndSizeQueryParams() throws Exception {
        when(taskService.getTaskHistory(any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        mockMvc.perform(get("/tasks/history?page=2&size=5")
                        .principal(new UsernamePasswordAuthenticationToken(USER_ID, null)))
                .andExpect(status().isOk());

        // Confirms the @PageableDefault(size = 20) on the controller doesn't
        // override an explicit ?page=&size= the client sent.
        verify(taskService).getTaskHistory(eq(PageRequest.of(2, 5)));
    }
}