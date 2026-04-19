package com.education.rpg.rpglearningbackend.dto;

import com.education.rpg.rpglearningbackend.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String avatarUrl;
    private Integer level;
    private Integer currentXp;
    private Integer gold;
    private Integer crystals;
    private Integer campfireLevel;
    private LocalDateTime lastLoginDate;
    private Integer energy;
    private LocalDateTime lastTaskCompletionDate;

    @JsonProperty("isPublicProfile")
    private Boolean isPublicProfile;

    private LocalDateTime xpBuffEndsAt;
    private LocalDateTime goldBuffEndsAt;
    private LocalDateTime energyStasisEndsAt;
    private Boolean hasActiveShield;

    private Integer lifetimeGold;
    private Integer lifetimeCrystals;
    private Integer totalTasksCompleted;
    private Integer totalFailures;
}
