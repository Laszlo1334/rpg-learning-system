package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String theoryContent;
    private String branchName;
    private Integer orderIndex;
    private Boolean isTheoryHidden;
    private Integer rewardXp;
    private Integer rewardGold;

    private Boolean isCompleted;
    private Boolean isLocked;
    private List<Long> prerequisiteTaskIds;

    private List<QuestionDto> questions;

    private String type; // Відправляємо як String (REGULAR, BOSS, MEMORY)
    private BossMetadata bossMetadata;
    private Integer dynamicQuestionCount;

    // Вкладений клас для метаданих
    @Data
    public static class BossMetadata {
        private String bossName;
        private String bossAvatar;
        private Integer timeLimitSeconds;
    }
}