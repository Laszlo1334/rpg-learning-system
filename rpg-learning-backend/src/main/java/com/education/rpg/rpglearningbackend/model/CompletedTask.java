package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "completed_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Integer attemptsTaken = 1;

    @Column(nullable = false)
    private Boolean hintsUsed = false;

    @Column(nullable = false)
    private Long timeSpentSeconds = 0L;

    public CompletedTask(User user, Task task) {
        this.user = user;
        this.task = task;
        this.completedAt = LocalDateTime.now();
        this.attemptsTaken = 1;
        this.hintsUsed = false;
        this.timeSpentSeconds = 0L;
    }
}
