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

    // --- ТВОРЇ СТАРІ БАЗОВІ ПОЛЯ (Повернули на місце) ---
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String avatarUrl; // Повернув, бо ти використовував це в DTO

    // --- БАЗОВА ЕКОНОМІКА ТА ПРОГРЕС ---
    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer currentXp = 0;

    public void setCurrentXp(Integer currentXp) {
        this.currentXp = currentXp;
        this.level = (currentXp / 1000) + 1;
    }


    @Column(nullable = false)
    private Integer gold = 0; // Поточний баланс монет (для Ачіверів)

    @Column(nullable = false)
    private Integer crystals = 0; // Валюта "Продуктивної невдачі"

    // --- МЕХАНІКА "БАГАТТЯ ТАБОРУ" (Streak) ---
    @Column(nullable = false)
    private Integer campfireLevel = 1; // Від 1 до 5

    private LocalDateTime lastLoginDate; // Для перевірки 48 годин бездіяльності

    // --- МЕХАНІКА "ЕНЕРГІЯ ВІДПОЧИНКУ" (Когнітивне навантаження) ---
    @Column(nullable = false)
    private Integer energy = 100; // Максимум 100

    private LocalDateTime lastTaskCompletionDate; // Для розрахунку відновлення (+1 за 6 хв)

    // --- ПРИВАТНІСТЬ (SDT: Автономія та Безпека) ---
    @Column(nullable = false)
    @JsonProperty("isPublicProfile")
    private Boolean isPublicProfile = true; // Opt-out система для Лідерборду

    // --- АКТИВНІ БАФИ ВІД ПРЕДМЕТІВ ---
    private LocalDateTime xpBuffEndsAt; // Еліксир Мудрості
    private LocalDateTime goldBuffEndsAt; // Магніт Гобліна
    private LocalDateTime energyStasisEndsAt; // Кава Магістра
    private Boolean hasActiveShield = false; // Аура Безстрашності (діє на 1 рівень)

    // --- ЛІТОПИС ГРАВЦЯ (Дані для дипломного дослідження) ---
    @Column(nullable = false)
    private Integer lifetimeGold = 0; // Все зароблене золото за весь час

    @Column(nullable = false)
    private Integer lifetimeCrystals = 0; // Всі отримані кристали

    @Column(nullable = false)
    private Integer totalTasksCompleted = 0;

    @Column(nullable = false)
    private Integer totalFailures = 0; // Ключова метрика для аналізу "Продуктивної невдачі"

    // --- Analytics: login & engagement ---
    @Column(nullable = false)
    private Integer totalLoginDays = 0;

    @Column(nullable = false)
    private Integer longestLoginStreak = 0;

    @Column(nullable = false)
    private Long totalPlayTimeSeconds = 0L;

    // --- Analytics: flawless task streaks ---
    @Column(nullable = false)
    private Integer currentFlawlessStreak = 0;

    @Column(nullable = false)
    private Integer longestFlawlessStreak = 0;
}