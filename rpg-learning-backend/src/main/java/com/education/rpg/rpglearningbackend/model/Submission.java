package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Column(columnDefinition = "TEXT")
    private String studentAnswer;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String teacherComment;

    // Номер спроби (для аналітики)
    private Integer attemptNumber = 1;

    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}