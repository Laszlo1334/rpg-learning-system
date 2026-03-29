package com.education.rpg.rpglearningbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResponse {
    private boolean isCorrect;        // true = зелений колір, false = червоний
    private String explanation;       // Текст підказки, якщо гравець помилився
    private Integer crystalsAwarded;  // Наприклад, 5 💎, якщо це перша унікальна помилка
    // Зверни увагу: ми не відправляємо правильну відповідь, лише пояснення!
}