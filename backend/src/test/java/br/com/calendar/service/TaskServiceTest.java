package br.com.calendar.service;

import br.com.calendar.domain.Task;
import br.com.calendar.domain.TaskMapper;
import br.com.calendar.domain.TaskRepository;
import br.com.calendar.domain.dto.TaskRequestDTO;
import br.com.calendar.domain.dto.TaskResponseDTO;
import br.com.calendar.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void updateTask_Success() {
        String taskId = "task123";
        TaskRequestDTO request = new TaskRequestDTO();
        Task existingTask = new Task();

        User owner = new User();
        owner.setId(new String("user123")); // objeto distinto, mesmo valor de ID
        existingTask.setUser(owner);

        User currentUser = new User();
        currentUser.setId(new String("user123")); // simula usuário vindo do contexto de autenticação

        when(repository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.updateTask(request, taskId));
    }

    @Test
    void updateTask_Unauthorized() {
        String taskId = "task123";
        String userId = "user123";
        String wrongUserId = "user456";
        TaskRequestDTO request = new TaskRequestDTO();
        Task existingTask = new Task();
        User user = new User();
        user.setId(userId);
        existingTask.setUser(user);

        User currentUser = new User();
        currentUser.setId(wrongUserId);

        when(repository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(request, taskId));
    }
}
