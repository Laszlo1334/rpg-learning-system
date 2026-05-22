package com.education.rpg.rpglearningbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String theoryContent;

    // Questions drawn for each roguelite run of this task
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Question> questions = new ArrayList<>();

    // Branch name used for the fog-of-war task map
    private String branchName;

    @Column(nullable = false)
    private Integer orderIndex;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_prerequisites", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "prerequisite_task_id")
    private List<Long> prerequisiteTaskIds = new ArrayList<>();

    // When true, theory is hidden (Mode B: discovery-first learning)
    @Column(nullable = false)
    private Boolean isTheoryHidden = false;

    // Rewards granted after all questions in the run are answered correctly
    private Integer rewardXp;
    private Integer rewardGold;

    public enum TaskType {
        REGULAR, BOSS, MEMORY
    }

    @Column(nullable = false)
    private Integer dynamicQuestionCount = 5;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type = TaskType.REGULAR;

    // Boss-only fields; null for REGULAR tasks
    private String bossName;
    private String bossAvatarUrl;
    private Integer timeLimitSeconds;
}