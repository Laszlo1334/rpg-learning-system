//package com.education.rpg.rpglearningbackend.controller;
//
//import com.education.rpg.rpglearningbackend.dto.SubmissionRequest;
//import com.education.rpg.rpglearningbackend.dto.SubmissionReviewRequest;
//import com.education.rpg.rpglearningbackend.model.Submission;
//import com.education.rpg.rpglearningbackend.service.SubmissionService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/submissions")
//@RequiredArgsConstructor
//@Tag(name = "Submissions", description = "Механіка здачі та перевірки квестів")
//public class SubmissionController {
//
//    private final SubmissionService submissionService;
//
//    @PostMapping
//    @Operation(summary = "Відправити відповідь на квест (Для студента)")
//    public ResponseEntity<?> submitTask(
//            @RequestBody SubmissionRequest request,
//            @AuthenticationPrincipal OAuth2User principal) {
//
//        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");
//
//        try {
//            String email = principal.getAttribute("email");
//            Submission result = submissionService.processSubmission(email, request);
//            return ResponseEntity.ok(result);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/{id}/review")
//    @Operation(summary = "Перевірити завдання (Тільки для Вчителя/Адміна)")
//    public ResponseEntity<?> reviewSubmission(
//            @PathVariable Long id,
//            @RequestBody SubmissionReviewRequest request,
//            @AuthenticationPrincipal OAuth2User principal) {
//
//        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");
//
//        try {
//            String reviewerEmail = principal.getAttribute("email");
//            Submission result = submissionService.reviewSubmission(id, reviewerEmail, request);
//            return ResponseEntity.ok(result);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//}