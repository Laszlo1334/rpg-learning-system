package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnswerRequest {
    private Long questionId;
    private List<String> selectedOptions;
    private String userAnswer;
}