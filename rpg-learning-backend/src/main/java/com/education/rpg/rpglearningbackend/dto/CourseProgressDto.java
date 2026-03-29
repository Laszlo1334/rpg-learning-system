package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;

@Data
public class CourseProgressDto {
    private Long id;
    private String title;
    private String description;

    // Метрики для прогрес-бару на фронтенді
    private int totalTasks;       // Скільки всього квестів у курсі
    private int completedTasks;   // Скільки квестів студент уже пройшов (APPROVED)
    private int progressPercentage; // Відсоток проходження (0-100)
}