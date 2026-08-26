package br.com.calendar.service;

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
import br.com.calendar.domain.Task;
import br.com.calendar.domain.TaskMapper;
import br.com.calendar.domain.TaskRepository;
import br.com.calendar.domain.dto.TaskRequestDTO;
import br.com.calendar.domain.dto.TaskResponseDTO;
import br.com.calendar.user.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final TaskMapper taskMapper;
    private final CategoryService categoryService;

    public Task createTask(Task task) {
        // Get the currently authenticated user from the security context
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        task.setUser(currentUser);
        return repository.save(task);
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

    public TaskResponseDTO updateTask(TaskRequestDTO task, String taskId) {
        Task existingTask = repository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User taskOwner = existingTask.getUser();

        if (!Objects.equals(currentUser.getId(), taskOwner.getId())) {
            throw new AccessDeniedException("Task does not belong to the current user");
        }

        if (!task.getAllDay() && task.getStartsAt().isAfter(task.getEndsAt())) {
            throw new IllegalArgumentException("Task start time must be before its end time.");
        }

        String categoryId = task.getCategoryId();
        if (categoryId != null) {
            Category category = categoryService.getCategoryOwnedByUser(categoryId, currentUser.getId());
            existingTask.setCategory(category);
        }

        taskMapper.updateEntity(existingTask, task);

        return taskMapper.toResponse(repository.save(existingTask));

    }

}