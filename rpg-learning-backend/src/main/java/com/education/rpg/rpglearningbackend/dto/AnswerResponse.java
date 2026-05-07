package com.education.rpg.rpglearningbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResponse {
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private String explanation;
    private Integer crystalsAwarded;
    @JsonProperty("isDead")
    private boolean isDead;
}