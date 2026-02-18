package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Отримати всі завдання для конкретного курсу
    List<Task> findByCourseId(Long courseId);
}