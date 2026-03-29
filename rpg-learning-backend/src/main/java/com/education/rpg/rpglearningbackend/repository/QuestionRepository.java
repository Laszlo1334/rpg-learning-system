package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
}