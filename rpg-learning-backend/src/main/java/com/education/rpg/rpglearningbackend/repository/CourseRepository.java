package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Знайти всі курси конкретного викладача
    List<Course> findByAuthorId(Long authorId);

    // Знайти курс за кодом доступу (для студентів)
    // Optional<Course> findByAccessCode(String accessCode); // Розкоментуй, коли знадобиться
}