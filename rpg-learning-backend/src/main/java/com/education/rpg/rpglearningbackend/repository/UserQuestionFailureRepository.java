package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.UserQuestionFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuestionFailureRepository extends JpaRepository<UserQuestionFailure, Long> {

    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
}
