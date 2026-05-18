package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class RunCompletionRequest {
    private Long taskId;
    
    @JsonProperty("isVictory")
    private boolean isVictory;
    
    private List<Long> failedQuestionIds;

    private Integer attemptsTaken;

    private Boolean hintsUsed;

    private Long timeSpentSeconds;
}