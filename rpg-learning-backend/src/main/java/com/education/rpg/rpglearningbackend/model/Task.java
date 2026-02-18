package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    // Нагороди
    private Integer rewardXp;
    private Integer rewardCoins;

    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    // Правильна відповідь (якщо тип AUTO)
    private String correctAnswer;
}