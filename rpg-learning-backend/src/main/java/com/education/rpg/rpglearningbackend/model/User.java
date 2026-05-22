package com.education.rpg.rpglearningbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;


    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String avatarUrl;


    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer currentXp = 0;

    public void setCurrentXp(Integer currentXp) {
        this.currentXp = currentXp;
        this.level = (currentXp / 1000) + 1;
    }


    @Column(nullable = false)
    private Integer gold = 0;

    // Crystals are earned through productive failure (wrong answers)
    @Column(nullable = false)
    private Integer crystals = 0;


    @Column(nullable = false)
    private Integer campfireLevel = 1; // Login streak bonus level (1–5)

    private LocalDateTime lastLoginDate; // Used to detect 48-hour inactivity and reset the streak


    @Column(nullable = false)
    private Integer energy = 100; // Cognitive-load throttle; max 100, restores over time

    private LocalDateTime lastTaskCompletionDate; // Used to calculate energy recovery (+1 per 6 min)


    @Column(nullable = false)
    @JsonProperty("isPublicProfile")
    private Boolean isPublicProfile = true; // Opt-out: false hides the user from the leaderboard

    // Active consumable buff expiry timestamps
    private LocalDateTime xpBuffEndsAt;
    private LocalDateTime goldBuffEndsAt;
    private LocalDateTime energyStasisEndsAt;
    private Boolean hasActiveShield = false; // Shield absorbs one defeat

    // Lifetime totals for research analytics
    @Column(nullable = false)
    private Integer lifetimeGold = 0; // Total gold ever earned (never decremented)

    @Column(nullable = false)
    private Integer lifetimeCrystals = 0; // Total crystals ever earned (never decremented)

    @Column(nullable = false)
    private Integer totalTasksCompleted = 0;

    @Column(nullable = false)
    private Integer totalFailures = 0; // Key metric for productive-failure research analysis

    // Login and engagement analytics
    @Column(nullable = false)
    private Integer totalLoginDays = 0;

    @Column(nullable = false)
    private Integer longestLoginStreak = 0;

    @Column(nullable = false)
    private Long totalPlayTimeSeconds = 0L;

    // Flawless-run streak analytics
    @Column(nullable = false)
    private Integer currentFlawlessStreak = 0;

    @Column(nullable = false)
    private Integer longestFlawlessStreak = 0;
}