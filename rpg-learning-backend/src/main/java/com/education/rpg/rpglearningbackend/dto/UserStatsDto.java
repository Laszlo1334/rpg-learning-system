package com.education.rpg.rpglearningbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsDto {
    private Integer lifetimeGold;
    private Integer lifetimeCrystals;
    private Integer totalTasksCompleted;
    private Integer totalFailures;
}