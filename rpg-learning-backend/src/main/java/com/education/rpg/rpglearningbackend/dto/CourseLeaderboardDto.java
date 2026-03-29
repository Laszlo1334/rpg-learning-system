package com.education.rpg.rpglearningbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseLeaderboardDto {
    private Long userId;
    private String username;
    private Long courseXp; // Spring Data JPA повертає суму (SUM) у вигляді Long
}