package com.education.rpg.rpglearningbackend.dto;

import lombok.Data;

@Data
public class SubmissionRequest {
    private Long taskId;
    private String answer;
}