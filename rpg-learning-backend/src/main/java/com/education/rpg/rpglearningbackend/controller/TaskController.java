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
@Tag(name = "Квести (Tasks)", description = "Керування завданнями для Дерева навичок")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Отримати всі квести", description = "Повертає список усіх доступних завдань у грі")
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати конкретний квест", description = "Повертає дані для старту забігу на Арені (із захистом)")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");
        try {
            String email = principal.getAttribute("email");
            return ResponseEntity.ok(taskService.getTaskById(id, email));
        } catch (RuntimeException e) {
            // Віддаємо 403 Forbidden, якщо гравець намагається відкрити заблокований квест
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Дерево навичок курсу", description = "Повертає завдання курсу зі статусами (пройдено/заблоковано) для поточного гравця")
    public ResponseEntity<List<TaskDto>> getTasksByCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).build();
        String email = principal.getAttribute("email");

        return ResponseEntity.ok(taskService.getTasksByCourseId(courseId, email));
    }

    @GetMapping("/memory")
    @Operation(summary = "Квест-Спогад", description = "Повертає випадкове завдання для інтервального повторення")
    public ResponseEntity<TaskDto> getMemoryTask(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        TaskDto memoryTask = taskService.getMemoryTask(email);

        if (memoryTask == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(memoryTask);
    }

    @PostMapping
    @Operation(summary = "Створити новий квест", description = "Додає нове завдання в базу даних (Для Вчителів)")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskService.createTask(task);
        return ResponseEntity.ok(savedTask);
    }
}