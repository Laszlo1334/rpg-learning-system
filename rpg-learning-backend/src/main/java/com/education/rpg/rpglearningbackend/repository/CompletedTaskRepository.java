package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.CompletedTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompletedTaskRepository extends JpaRepository<CompletedTask, Long> {

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    // Знайти всі завершені завдання гравця, які були пройдені раніше вказаної дати
    List<CompletedTask> findByUserIdAndCompletedAtBefore(Long userId, LocalDateTime date);

    // ДОДАНО: Знайти всі завершені завдання конкретного гравця
    List<CompletedTask> findByUserId(Long userId);

    int countByUserIdAndTaskCourseId(Long userId, Long courseId);
}