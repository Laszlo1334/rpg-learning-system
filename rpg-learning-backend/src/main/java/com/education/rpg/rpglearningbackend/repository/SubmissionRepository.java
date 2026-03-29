package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Submission;
import com.education.rpg.rpglearningbackend.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Знайти всі відповіді конкретного студента
    List<Submission> findByStudentId(Long studentId);

    // Знайти всі роботи, які чекають перевірки (для викладача)
    List<Submission> findByTaskIdAndStatus(Long taskId, SubmissionStatus status);

    // 1. Перевіряє, чи взагалі пробував студент це завдання (для видачі Кристалів тільки за ПЕРШУ помилку)
    boolean existsByTaskIdAndStudentId(Long taskId, Long studentId);

    // 2. Перевіряє, чи студент вже УСПІШНО пройшов це завдання (щоб не давати XP та Золото вдруге)
    boolean existsByTaskIdAndStudentIdAndStatus(Long taskId, Long studentId, SubmissionStatus status);
}