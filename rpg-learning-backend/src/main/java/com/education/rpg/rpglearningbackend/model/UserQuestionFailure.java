package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_failures",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuestionFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Question question;

    private LocalDateTime failedAt;

    @Column(nullable = false)
    private Integer failureCount = 1;

    @Column(length = 1024)
    private String lastWrongAnswer;

    @PrePersist
    protected void onCreate() {
        if (this.failedAt == null) {
            this.failedAt = LocalDateTime.now();
        }
        if (this.failureCount == null) {
            this.failureCount = 1;
        }
    }
}
