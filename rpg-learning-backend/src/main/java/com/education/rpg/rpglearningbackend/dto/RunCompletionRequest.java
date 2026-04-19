package com.education.rpg.rpglearningbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RunCompletionRequest {
    private Long taskId;                  // Яке завдання (підземелля) проходив гравець

    @JsonProperty("isVictory")
    private boolean isVictory;            // true = перемога (є ❤️), false = Game Over (немає ❤️)

    private List<Long> failedQuestionIds; // ID запитань, де були помилки (для статистики)
}