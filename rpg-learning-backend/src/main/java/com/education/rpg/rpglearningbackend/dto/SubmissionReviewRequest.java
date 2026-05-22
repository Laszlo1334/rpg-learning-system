package com.education.rpg.rpglearningbackend.dto;

import com.education.rpg.rpglearningbackend.model.SubmissionStatus;
import lombok.Data;

@Data
public class SubmissionReviewRequest {
    private SubmissionStatus status;
    private String teacherComment;
}