package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.CompletedTask;
import com.education.rpg.rpglearningbackend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Bulk-deletes all completed_tasks records that reference any of the given tasks.
     * Called by DatabaseSeeder before removing a course to satisfy the FK constraint
     * on completed_tasks.task_id without altering the DDL schema.
     * A single bulk DELETE is used instead of derived-delete to avoid SELECT + N removes.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM CompletedTask ct WHERE ct.task IN :tasks")
    void deleteByTaskIn(@Param("tasks") List<Task> tasks);
}