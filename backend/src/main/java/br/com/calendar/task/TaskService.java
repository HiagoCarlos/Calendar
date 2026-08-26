package br.com.calendar.task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.calendar.category.Category;
import br.com.calendar.category.CategoryService;
import br.com.calendar.common.exception.ResourceNotFoundException;
import br.com.calendar.task.dto.TaskRequestDTO;
import br.com.calendar.task.dto.TaskResponseDTO;
import br.com.calendar.user.User;
import br.com.calendar.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final TaskMapper taskMapper;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public TaskResponseDTO createTask(TaskRequestDTO request) {
        User currentUser = currentUser();

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryOwnedByUser(request.getCategoryId(), currentUser.getId());
        }

        Task task = taskMapper.toEntity(request, category);
        task.setUser(currentUser);

        validateTaskDates(task);

        return taskMapper.toResponse(repository.save(task));
    }

    public List<TaskResponseDTO> getTasksForDay(LocalDate date) {
        String userId = currentUser().getId();
        var startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        var endOfDay = date.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
        return repository.findActiveTasksForDay(userId, startOfDay, endOfDay).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public void deleteTask(String id) {
        repository.findByIdAndDeletedAtIsNull(id).ifPresent(task -> {
            checkOwnership(task);
            repository.delete(task);
        });
    }

    public List<TaskResponseDTO> getTaskHistory() {
        String userId = currentUser().getId();
        return repository.findAllByUser_Id(userId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    /**
     * PATCH semantics: fields omitted from the request are left unchanged.
     */
    public TaskResponseDTO updateTask(TaskRequestDTO request, String taskId) {
        Task existingTask = findOwnedTask(taskId);
        User currentUser = currentUser();

        if (request.getCategoryId() != null) {
            existingTask.setCategory(categoryService.getCategoryOwnedByUser(request.getCategoryId(), currentUser.getId()));
        }

        taskMapper.updateEntity(existingTask, request);

        validateTaskDates(existingTask);

        return taskMapper.toResponse(repository.save(existingTask));
    }

    /**
     * PUT semantics: full replacement. Fields omitted from the request clear
     * the corresponding value on the task, including the category.
     */
    public TaskResponseDTO replaceTask(TaskRequestDTO request, String taskId) {
        Task existingTask = findOwnedTask(taskId);
        User currentUser = currentUser();

        Category category = request.getCategoryId() != null
                ? categoryService.getCategoryOwnedByUser(request.getCategoryId(), currentUser.getId())
                : null;
        existingTask.setCategory(category);

        taskMapper.replaceEntity(existingTask, request);

        validateTaskDates(existingTask);

        return taskMapper.toResponse(repository.save(existingTask));
    }

    private Task findOwnedTask(String taskId) {
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        checkOwnership(task);

        return task;
    }

    private void checkOwnership(Task task) {
        User currentUser = currentUser();
        if (!Objects.equals(currentUser.getId(), task.getUser().getId())) {
            throw new AccessDeniedException("Task does not belong to the current user");
        }
    }

    /**
     * The JwtFilter authenticates with a Spring Security UserDetails, not our
     * domain User — the authenticated user's id is its name/username, not
     * its principal, so it must be looked up.
     */
    private User currentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateTaskDates(Task task) {
        boolean allDay = Boolean.TRUE.equals(task.getAllDay());

        if (!allDay && task.getStartsAt() != null && task.getEndsAt() != null
                && task.getStartsAt().isAfter(task.getEndsAt())) {
            throw new IllegalArgumentException("Task start time must be before its end time.");
        }
    }
}
