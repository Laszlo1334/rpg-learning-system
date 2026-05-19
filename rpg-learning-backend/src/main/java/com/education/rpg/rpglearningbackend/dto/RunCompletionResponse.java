package com.education.rpg.rpglearningbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunCompletionResponse {
    private String message;
    private int earnedXp;
    private int earnedGold;
    private int baseXp;
    private int baseGold;
    private double flawlessMultiplier;
    private double campfireMultiplier;
    private double xpBuffMultiplier;
    private double goldBuffMultiplier;
    private double energyMultiplier;
}
