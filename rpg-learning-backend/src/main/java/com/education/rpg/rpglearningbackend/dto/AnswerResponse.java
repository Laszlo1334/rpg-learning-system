package com.education.rpg.rpglearningbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResponse {
    private boolean isCorrect;
    private String explanation;
    private Integer crystalsAwarded;
    private boolean isDead;
}