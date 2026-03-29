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

    // --- НОВЕ: Список запитань для цього забігу (Roguelite цикл) ---
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Question> questions = new ArrayList<>();

    // --- МЕХАНІКА ГІЛОК ТА "ТУМАНУ ВІЙНИ" ---
    private String branchName;

    @Column(nullable = false)
    private Integer orderIndex;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_prerequisites", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "prerequisite_task_id")
    private List<Long> prerequisiteTaskIds = new ArrayList<>();

    // --- РЕЖИМИ НАВЧАННЯ (Управління когнітивним навантаженням) ---
    @Column(nullable = false)
    private Boolean isTheoryHidden = false; // true = Режим Б (теорії немає)

    // Нагороди за весь забіг (видаються після успішного проходження всіх запитань)
    private Integer rewardXp;
    private Integer rewardGold;
}