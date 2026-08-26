package br.com.calendar.task;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.calendar.task.dto.TaskRequestDTO;
import br.com.calendar.task.dto.TaskResponseDTO;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskService service;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskRequestDTO task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTask(task));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasksByDay(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getTasksForDay(date));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<TaskResponseDTO>> getTaskHistory() {
        return ResponseEntity.ok(service.getTaskHistory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> replaceTask(@PathVariable String id, @RequestBody TaskRequestDTO task) {
        return ResponseEntity.ok(service.replaceTask(task, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> patchTask(@PathVariable String id, @RequestBody TaskRequestDTO task) {
        return ResponseEntity.ok(service.updateTask(task, id));
    }
}
