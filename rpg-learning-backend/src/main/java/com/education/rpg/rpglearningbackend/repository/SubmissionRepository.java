package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Submission;
import com.education.rpg.rpglearningbackend.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentId(Long studentId);

    List<Submission> findByTaskIdAndStatus(Long taskId, SubmissionStatus status);

    // True if the student has any prior attempt — used to award crystals only on the first failure
    boolean existsByTaskIdAndStudentId(Long taskId, Long studentId);

    // True if the student already passed this task — prevents awarding XP/Gold a second time
    boolean existsByTaskIdAndStudentIdAndStatus(Long taskId, Long studentId, SubmissionStatus status);
}