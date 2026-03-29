package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class RunCompletionRequest {
    private Long taskId;                 // Яке завдання (підземелля) проходив гравець
    private boolean isVictory;           // true = перемога (є ❤️), false = Game Over (немає ❤️)
    private List<Long> failedQuestionIds; // ID запитань, де були помилки (для статистики)
}