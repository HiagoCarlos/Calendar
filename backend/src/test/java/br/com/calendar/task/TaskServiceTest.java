package br.com.calendar.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.calendar.category.Category;
import br.com.calendar.category.CategoryService;
import br.com.calendar.common.exception.ResourceNotFoundException;
import br.com.calendar.task.dto.TaskRequestDTO;
import br.com.calendar.task.dto.TaskResponseDTO;
import br.com.calendar.user.User;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private CategoryService categoryService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private static final String TASK_ID = "task123";
    private static final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    private TaskRequestDTO validRequest() {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setAllDay(false);
        request.setStartsAt(Instant.now());
        request.setEndsAt(Instant.now().plus(1, ChronoUnit.HOURS));
        return request;
    }

    private User userWithId(String id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private void mockAuthenticatedUser(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
    }

    @Test
    void createTask_Success() {
        TaskRequestDTO request = validRequest();

        Task mappedTask = new Task();

        mockAuthenticatedUser(userWithId(USER_ID));
        when(taskMapper.toEntity(request, null)).thenReturn(mappedTask);
        when(repository.save(mappedTask)).thenReturn(mappedTask);
        when(taskMapper.toResponse(mappedTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.createTask(request));

        assertEquals(USER_ID, mappedTask.getUser().getId());
        verify(repository).save(mappedTask);
    }

    @Test
    void createTask_InvalidDates_ThrowsIllegalArgumentException() {
        TaskRequestDTO request = validRequest();
        request.setStartsAt(Instant.now());
        request.setEndsAt(Instant.now().minus(1, ChronoUnit.HOURS));

        Task mappedTask = new Task();
        mappedTask.setAllDay(request.getAllDay());
        mappedTask.setStartsAt(request.getStartsAt());
        mappedTask.setEndsAt(request.getEndsAt());

        mockAuthenticatedUser(userWithId(USER_ID));
        when(taskMapper.toEntity(request, null)).thenReturn(mappedTask);

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(request));

        verify(repository, never()).save(any());
    }

    @Test
    void createTask_WithCategory_ValidatesOwnershipAndSetsCategory() {
        TaskRequestDTO request = validRequest();
        request.setCategoryId("cat123");

        Category category = new Category();
        category.setId("cat123");

        Task mappedTask = new Task();

        mockAuthenticatedUser(userWithId(USER_ID));
        when(categoryService.getCategoryOwnedByUser("cat123", USER_ID)).thenReturn(category);
        when(taskMapper.toEntity(request, category)).thenReturn(mappedTask);
        when(repository.save(mappedTask)).thenReturn(mappedTask);
        when(taskMapper.toResponse(mappedTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.createTask(request));

        verify(categoryService).getCategoryOwnedByUser("cat123", USER_ID);
    }

    @Test
    void createTask_CategoryNotOwnedByUser_ThrowsAccessDenied() {
        TaskRequestDTO request = validRequest();
        request.setCategoryId("cat999");

        mockAuthenticatedUser(userWithId(USER_ID));
        when(categoryService.getCategoryOwnedByUser("cat999", USER_ID))
                .thenThrow(new AccessDeniedException("Category does not belong to the current user"));

        assertThrows(AccessDeniedException.class, () -> taskService.createTask(request));

        verify(repository, never()).save(any());
    }

    @Test
    void updateTask_Success() {
        TaskRequestDTO request = validRequest();

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        User currentUser = userWithId(USER_ID);

        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        mockAuthenticatedUser(currentUser);
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.updateTask(request, TASK_ID));
        verify(taskMapper).updateEntity(existingTask, request);
    }

    @Test
    void updateTask_Unauthorized_WhenUserIsNotOwner() {
        TaskRequestDTO request = validRequest();

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        User currentUser = userWithId("user456");

        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        mockAuthenticatedUser(currentUser);

        assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(request, TASK_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void updateTask_TaskNotFound_ThrowsResourceNotFoundException() {
        TaskRequestDTO request = validRequest();

        when(repository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.updateTask(request, TASK_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void updateTask_PartialUpdate_KeepsExistingDatesValid() {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Novo título");

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));
        existingTask.setAllDay(false);
        existingTask.setStartsAt(Instant.now());
        existingTask.setEndsAt(Instant.now().plus(1, ChronoUnit.HOURS));

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        doAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setTitle("Novo título"); // simula o merge real do mapper
            return null;
        }).when(taskMapper).updateEntity(eq(existingTask), eq(request));

        assertNotNull(taskService.updateTask(request, TASK_ID));
    }

    @Test
    void updateTask_PartialUpdate_InvalidatesExistingDates() {
        // PATCH que muda só o endsAt, tornando o intervalo inválido
        TaskRequestDTO request = new TaskRequestDTO();
        request.setEndsAt(Instant.now().minus(1, ChronoUnit.DAYS));

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));
        existingTask.setAllDay(false);
        existingTask.setStartsAt(Instant.now());
        existingTask.setEndsAt(Instant.now().plus(1, ChronoUnit.HOURS));

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));

        doAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setEndsAt(request.getEndsAt()); // simula o merge real
            return null;
        }).when(taskMapper).updateEntity(eq(existingTask), eq(request));

        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTask(request, TASK_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void updateTask_AllDayNullOnEntity_TreatedAsNotAllDay() {
        TaskRequestDTO request = validRequest(); // allDay=false, startsAt antes de endsAt

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));
        existingTask.setAllDay(null); // estado inconsistente propositalmente

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));

        doAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setAllDay(request.getAllDay());
            t.setStartsAt(request.getStartsAt());
            t.setEndsAt(request.getEndsAt());
            return null;
        }).when(taskMapper).updateEntity(eq(existingTask), eq(request));

        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertDoesNotThrow(() -> taskService.updateTask(request, TASK_ID));
    }

    @Test
    void updateTask_AllDayTrue_SkipsDateValidation() {
        TaskRequestDTO request = validRequest();
        request.setAllDay(true);
        request.setStartsAt(Instant.now());
        request.setEndsAt(Instant.now().minus(1, ChronoUnit.HOURS)); // seria inválido se allDay=false

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.updateTask(request, TASK_ID));
    }

    @Test
    void updateTask_WithCategory_ValidatesOwnershipAndSetsCategory() {
        TaskRequestDTO request = validRequest();
        request.setCategoryId("cat123");

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        Category category = new Category();
        category.setId("cat123");

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(categoryService.getCategoryOwnedByUser("cat123", USER_ID)).thenReturn(category);
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.updateTask(request, TASK_ID));

        assertEquals(category, existingTask.getCategory());
        verify(categoryService).getCategoryOwnedByUser("cat123", USER_ID);
    }

    @Test
    void updateTask_CategoryNotOwnedByUser_ThrowsAccessDenied() {
        TaskRequestDTO request = validRequest();
        request.setCategoryId("cat999");

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(categoryService.getCategoryOwnedByUser("cat999", USER_ID))
                .thenThrow(new AccessDeniedException("Category does not belong to the current user"));

        assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(request, TASK_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void replaceTask_OmittedFields_ClearsThemOnTheTask() {
        // PUT sem title/description: full replacement deve limpar os campos, não preservá-los
        TaskRequestDTO request = validRequest();

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));
        existingTask.setTitle("Título antigo");
        existingTask.setDescription("Descrição antiga");

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        doAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            TaskRequestDTO r = invocation.getArgument(1);
            t.setTitle(r.getTitle());
            t.setDescription(r.getDescription());
            return null;
        }).when(taskMapper).replaceEntity(eq(existingTask), eq(request));

        assertNotNull(taskService.replaceTask(request, TASK_ID));

        verify(taskMapper).replaceEntity(existingTask, request);
        verify(taskMapper, never()).updateEntity(any(), any());
        assertEquals(null, existingTask.getTitle());
        assertEquals(null, existingTask.getDescription());
    }

    @Test
    void replaceTask_OmittedCategoryId_ClearsCategory() {
        TaskRequestDTO request = validRequest();

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));
        existingTask.setCategory(new Category());

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.replaceTask(request, TASK_ID));

        assertEquals(null, existingTask.getCategory());
        verifyNoInteractions(categoryService);
    }

    @Test
    void replaceTask_WithCategory_ValidatesOwnershipAndSetsCategory() {
        TaskRequestDTO request = validRequest();
        request.setCategoryId("cat123");

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        Category category = new Category();
        category.setId("cat123");

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(categoryService.getCategoryOwnedByUser("cat123", USER_ID)).thenReturn(category);
        when(repository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(TaskResponseDTO.builder().build());

        assertNotNull(taskService.replaceTask(request, TASK_ID));

        assertEquals(category, existingTask.getCategory());
        verify(categoryService).getCategoryOwnedByUser("cat123", USER_ID);
    }

    @Test
    void replaceTask_Unauthorized_WhenUserIsNotOwner() {
        TaskRequestDTO request = validRequest();

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        mockAuthenticatedUser(userWithId("user456"));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));

        assertThrows(AccessDeniedException.class,
                () -> taskService.replaceTask(request, TASK_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void replaceTask_InvalidDates_ThrowsIllegalArgumentException() {
        TaskRequestDTO request = validRequest();
        request.setStartsAt(Instant.now());
        request.setEndsAt(Instant.now().minus(1, ChronoUnit.HOURS));

        Task existingTask = new Task();
        existingTask.setUser(userWithId(USER_ID));

        mockAuthenticatedUser(userWithId(USER_ID));
        when(repository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));

        doAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            TaskRequestDTO r = invocation.getArgument(1);
            t.setAllDay(r.getAllDay());
            t.setStartsAt(r.getStartsAt());
            t.setEndsAt(r.getEndsAt());
            return null;
        }).when(taskMapper).replaceEntity(eq(existingTask), eq(request));

        assertThrows(IllegalArgumentException.class,
                () -> taskService.replaceTask(request, TASK_ID));

        verify(repository, never()).save(any());
    }
}
