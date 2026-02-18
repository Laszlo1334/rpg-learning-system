package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Submission;
import com.education.rpg.rpglearningbackend.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Знайти всі відповіді конкретного студента
    List<Submission> findByStudentId(Long studentId);

    // Знайти відповідь студента на конкретне завдання (щоб перевірити, чи він вже здав)
    Optional<Submission> findByTaskIdAndStudentId(Long taskId, Long studentId);

    // Знайти всі роботи, які чекають перевірки (для викладача)
    List<Submission> findByTaskIdAndStatus(Long taskId, SubmissionStatus status);
}