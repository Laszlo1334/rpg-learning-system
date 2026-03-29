package com.education.rpg.rpglearningbackend.dto;

import com.education.rpg.rpglearningbackend.model.Question.QuestionType;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDto {
    private Long id;
    private String questionText;
    private QuestionType type;
    private List<String> options;

}