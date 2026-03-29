package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Поки що стандартних методів JpaRepository тут достатньо
}