package br.com.calendar.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.calendar.domain.Task;
import br.com.calendar.domain.dto.TaskRequestDTO;
import br.com.calendar.domain.dto.TaskResponseDTO;
import br.com.calendar.service.TaskService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskService service;

    @GetMapping
    public ResponseEntity<List<Task>> getTasksByDay(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getTasksForDay(date));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<Task>> getTaskHistory() {
        return ResponseEntity.ok(service.getTaskHistory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> replaceTask(@PathVariable String id, @RequestBody TaskRequestDTO task) {
        return ResponseEntity.ok(service.updateTask(task, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> patchTask(@PathVariable String id, @RequestBody TaskRequestDTO task) {
        return ResponseEntity.ok(service.updateTask(task, id));
    }
}