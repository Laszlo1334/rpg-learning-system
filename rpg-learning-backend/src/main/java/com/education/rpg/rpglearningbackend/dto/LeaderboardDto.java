package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;

@Data
public class LeaderboardDto {
    private Long id;
    private String username;
    private Integer level;
    private Long xp;
    private String avatarUrl;
}