package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "completed_tasks")
@Data
@NoArgsConstructor
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

    public CompletedTask(User user, Task task) {
        this.user = user;
        this.task = task;
        this.completedAt = LocalDateTime.now();
    }
}