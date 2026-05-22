package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCourseId(Long courseId);

    List<Task> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    int countByCourseId(Long courseId);
}