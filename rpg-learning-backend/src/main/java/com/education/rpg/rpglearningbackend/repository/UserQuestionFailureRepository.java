package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Question;
import com.education.rpg.rpglearningbackend.model.UserQuestionFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserQuestionFailureRepository extends JpaRepository<UserQuestionFailure, Long> {

    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);

    Optional<UserQuestionFailure> findByUserIdAndQuestionId(Long userId, Long questionId);

    int countByUserIdAndQuestion_TaskId(Long userId, Long taskId);

    /**
     * Bulk-deletes failure records for the given questions.
     * Called by DatabaseSeeder before dropping a course to satisfy the FK constraint
     * on user_question_failures.question_id.
     */
    @Transactional
    void deleteByQuestionIn(List<Question> questions);
}
