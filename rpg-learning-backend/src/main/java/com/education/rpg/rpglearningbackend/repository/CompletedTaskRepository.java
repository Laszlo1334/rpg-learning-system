package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.CompletedTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompletedTaskRepository extends JpaRepository<CompletedTask, Long> {

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    // Find all tasks completed by a player before the given date (used for spaced repetition)
    List<CompletedTask> findByUserIdAndCompletedAtBefore(Long userId, LocalDateTime date);

    // Find all completed tasks for a specific player
    List<CompletedTask> findByUserId(Long userId);

    int countByUserIdAndTaskCourseId(Long userId, Long courseId);
}