package com.education.rpg.rpglearningbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseLeaderboardDto {
    private Long userId;
    private String username;
    private Long courseXp; // Spring Data JPA returns the SUM as Long
    private String avatarUrl;

    public CourseLeaderboardDto(Long userId, String username, Long courseXp) {
        this.userId = userId;
        this.username = username;
        this.courseXp = courseXp;
    }
}