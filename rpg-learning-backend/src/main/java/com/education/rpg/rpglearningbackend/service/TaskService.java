package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Отримати всі квести
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Створити новий квест
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
}