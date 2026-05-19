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
     * Bulk-deletes all failure records whose question is in the given list.
     * Called by DatabaseSeeder before removing a course to satisfy the FK constraint
     * on user_question_failures.question_id without altering the DDL schema.
     *
     * @Transactional is required because Spring Data JPA derived delete methods
     * perform a SELECT + individual removes under the hood, which needs an active
     * persistence context / transaction to function correctly.
     */
    @Transactional
    void deleteByQuestionIn(List<Question> questions);
}
