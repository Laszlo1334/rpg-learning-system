package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Квести (Tasks)", description = "Керування завданнями для гравців")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Отримати всі квести", description = "Повертає список усіх доступних завдань у грі")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    @Operation(summary = "Створити новий квест", description = "Додає нове завдання в базу даних (В ідеалі - тільки для Вчителів)")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskService.createTask(task);
        return ResponseEntity.ok(savedTask);
    }
}