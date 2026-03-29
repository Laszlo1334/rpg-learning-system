package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.AnswerRequest;
import com.education.rpg.rpglearningbackend.dto.AnswerResponse;
import com.education.rpg.rpglearningbackend.dto.RunCompletionRequest;
import com.education.rpg.rpglearningbackend.model.Question;
import com.education.rpg.rpglearningbackend.repository.QuestionRepository;
import com.education.rpg.rpglearningbackend.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/arena")
@RequiredArgsConstructor
public class ArenaController {

    private final QuestionRepository questionRepository;
    private final SubmissionService submissionService;

    @PostMapping("/check-answer")
    public ResponseEntity<AnswerResponse> checkAnswer(@RequestBody AnswerRequest request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Запитання не знайдено"));

        boolean isCorrect = question.getCorrectAnswers().stream()
                .anyMatch(correct -> correct.trim().equalsIgnoreCase(request.getUserAnswer().trim()));

        AnswerResponse response = AnswerResponse.builder()
                .isCorrect(isCorrect)
                .explanation(isCorrect ? null : question.getExplanation())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish-run")
    public ResponseEntity<String> finishRun(@RequestBody RunCompletionRequest request,
                                            @AuthenticationPrincipal OAuth2User principal) {
        // Беремо email гравця з його Google-сесії
        String email = principal.getAttribute("email");
        submissionService.processRunCompletion(email, request);
        return ResponseEntity.ok("Результати забігу збережено!");
    }
}