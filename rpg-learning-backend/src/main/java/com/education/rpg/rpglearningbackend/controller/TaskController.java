package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.TaskDto;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management for the Skill Tree")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns a list of all available tasks in the game")
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // IMPORTANT: /course/{courseId} must be declared BEFORE /{id}.
    // Spring matches routes top-to-bottom; if /{id} comes first, the literal path segment
    // "course" is parsed as a Long, causing a 400/404 conversion error.

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Course Skill Tree", description = "Returns course tasks with completed/locked statuses for the current player")
    public ResponseEntity<List<TaskDto>> getTasksByCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        String email = principal.getAttribute("email");
        return ResponseEntity.ok(taskService.getTasksByCourseId(courseId, email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific task", description = "Returns task data for starting an Arena run (with lock enforcement)")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).body("Please log in!");
        try {
            String email = principal.getAttribute("email");
            return ResponseEntity.ok(taskService.getTaskById(id, email));
        } catch (RuntimeException e) {
            // Return 403 Forbidden if the player tries to open a locked task
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Adds a new task to the database (for Teachers)")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskService.createTask(task);
        return ResponseEntity.ok(savedTask);
    }
}