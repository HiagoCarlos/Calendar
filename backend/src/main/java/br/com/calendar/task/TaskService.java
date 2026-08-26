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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final TaskMapper taskMapper;
    private final CategoryService categoryService;

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

    public List<Task> getTasksForDay(LocalDate date) {
        var startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        var endOfDay = date.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
        return repository.findActiveTasksForDay(startOfDay, endOfDay);
    }

    public void deleteTask(String id) {
        repository.findByIdAndDeletedAtIsNull(id)
                .ifPresent(task -> repository.deleteById(id));
    }

    public List<Task> getTaskHistory() {
        return repository.findAll();
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

        User currentUser = currentUser();
        if (!Objects.equals(currentUser.getId(), task.getUser().getId())) {
            throw new AccessDeniedException("Task does not belong to the current user");
        }

        return task;
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void validateTaskDates(Task task) {
        boolean allDay = Boolean.TRUE.equals(task.getAllDay());

        if (!allDay && task.getStartsAt() != null && task.getEndsAt() != null
                && task.getStartsAt().isAfter(task.getEndsAt())) {
            throw new IllegalArgumentException("Task start time must be before its end time.");
        }
    }
}
