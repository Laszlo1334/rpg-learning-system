package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long questionId;
    private String userAnswer;
}