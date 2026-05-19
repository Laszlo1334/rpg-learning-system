package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.AnswerRequest;
import com.education.rpg.rpglearningbackend.dto.AnswerResponse;
import com.education.rpg.rpglearningbackend.dto.RunCompletionRequest;
import com.education.rpg.rpglearningbackend.dto.RunCompletionResponse;
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

    private final SubmissionService submissionService;

    @PostMapping("/check-answer")
    public ResponseEntity<AnswerResponse> checkAnswer(@RequestBody AnswerRequest request,
                                                      @AuthenticationPrincipal OAuth2User principal) {
        String email = principal != null ? principal.getAttribute("email") : null;
        AnswerResponse response = submissionService.checkAnswerAndProcessFailure(
                request.getQuestionId(),
                request.getUserAnswer(),
                email
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish-run")
    public ResponseEntity<RunCompletionResponse> finishRun(@RequestBody RunCompletionRequest request,
                                            @AuthenticationPrincipal OAuth2User principal) {
        // Беремо email гравця з його Google-сесії
        String email = principal.getAttribute("email");
        RunCompletionResponse response = submissionService.processRunCompletion(email, request);
        return ResponseEntity.ok(response);
    }
}