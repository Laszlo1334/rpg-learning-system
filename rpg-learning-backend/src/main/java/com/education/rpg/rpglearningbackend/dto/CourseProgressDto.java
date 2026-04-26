package com.education.rpg.rpglearningbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressDto {
    private Long id;
    private String title;
    private String description;
    private int totalTasks;
    private int completedTasks;
    private String status;
}