package br.com.calendar.domain;

import org.springframework.stereotype.Component;

import br.com.calendar.category.Category;
import br.com.calendar.category.CategoryRepository;
import br.com.calendar.common.exception.ResourceNotFoundException;
import br.com.calendar.domain.dto.TaskRequestDTO;
import br.com.calendar.domain.dto.TaskResponseDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final CategoryRepository categoryRepository;

    public Task toEntity(TaskRequestDTO request) {
        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTimezone(request.getTimezone());
        task.setLocation(request.getLocation());
        task.setStatus(request.getStatus());
        task.setStartsAt(request.getStartsAt());
        task.setEndsAt(request.getEndsAt());
        task.setRepeat(request.getRepeat());
        task.setRepeatInterval(request.getRepeatInterval());
        task.setAllDay(request.getAllDay());
        task.setCompletedAt(request.getCompletedAt());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            task.setCategory(category);
        }
        
        return task;
    }

    public TaskResponseDTO toResponse(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .timezone(task.getTimezone())
                .location(task.getLocation())
                .status(task.getStatus())
                .startsAt(task.getStartsAt())
                .endsAt(task.getEndsAt())
                .repeat(task.getRepeat())
                .repeatInterval(task.getRepeatInterval())
                .allDay(task.getAllDay())
                .categoryId(
                        task.getCategory() != null
                                ? task.getCategory().getId()
                                : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

    public void updateEntity(Task task, TaskRequestDTO request) {
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTimezone() != null) {
            task.setTimezone(request.getTimezone());
        }
        if (request.getLocation() != null) {
            task.setLocation(request.getLocation());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getStartsAt() != null) {
            task.setStartsAt(request.getStartsAt());
        }
        if (request.getEndsAt() != null) {
            task.setEndsAt(request.getEndsAt());
        }
        if (request.getRepeat() != null) {
            task.setRepeat(request.getRepeat());
        }
        if (request.getRepeatInterval() != null) {
            task.setRepeatInterval(request.getRepeatInterval());
        }
        if (request.getAllDay() != null) {
            task.setAllDay(request.getAllDay());
        }
        if (request.getCompletedAt() != null) {
            task.setCompletedAt(request.getCompletedAt());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            task.setCategory(category);
        }
    }
}
