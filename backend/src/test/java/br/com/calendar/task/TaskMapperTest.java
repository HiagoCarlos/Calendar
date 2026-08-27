package br.com.calendar.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.calendar.category.Category;
import br.com.calendar.task.dto.TaskRequestDTO;
import br.com.calendar.task.dto.TaskResponseDTO;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    void toEntity_IgnoresCompletedAtFromRequest() {
        // A newly created task must always start incomplete, regardless of
        // what the client sends — see issue #110's acceptance criteria.
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Task");
        request.setCompletedAt(Instant.now());

        Task task = mapper.toEntity(request, null);

        assertNull(task.getCompletedAt());
    }

    @Test
    void toEntity_MapsPriority() {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Task");
        request.setPriority(TaskPriority.high);

        Task task = mapper.toEntity(request, null);

        assertEquals(TaskPriority.high, task.getPriority());
    }

    @Test
    void toEntity_SetsCategory() {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Task");
        Category category = new Category();
        category.setId("cat123");

        Task task = mapper.toEntity(request, category);

        assertEquals(category, task.getCategory());
    }

    @Test
    void toResponse_MapsPriority() {
        Task task = new Task();
        task.setPriority(TaskPriority.medium);

        TaskResponseDTO response = mapper.toResponse(task);

        assertEquals(TaskPriority.medium, response.getPriority());
    }

    @Test
    void updateEntity_OmittedPriority_LeavesItUnchanged() {
        Task task = new Task();
        task.setPriority(TaskPriority.low);

        mapper.updateEntity(task, new TaskRequestDTO());

        assertEquals(TaskPriority.low, task.getPriority());
    }

    @Test
    void updateEntity_SetPriority_UpdatesIt() {
        Task task = new Task();
        task.setPriority(TaskPriority.low);

        TaskRequestDTO request = new TaskRequestDTO();
        request.setPriority(TaskPriority.high);
        mapper.updateEntity(task, request);

        assertEquals(TaskPriority.high, task.getPriority());
    }

    @Test
    void replaceEntity_OmittedPriority_ClearsIt() {
        Task task = new Task();
        task.setPriority(TaskPriority.low);

        mapper.replaceEntity(task, new TaskRequestDTO());

        assertNull(task.getPriority());
    }
}
