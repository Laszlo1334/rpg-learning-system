package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.CourseProgressDto;
import com.education.rpg.rpglearningbackend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Глобальна Карта (Courses)", description = "Макронавчання: курси та загальний прогрес")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Отримати всі курси", description = "Повертає список курсів із вирахованим прогресом гравця")
    public ResponseEntity<?> getAllCourses(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");

        String email = principal.getAttribute("email");
        List<CourseProgressDto> courses = courseService.getAllCoursesWithProgress(email);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Увійти в Підземелля", description = "Повертає інформацію про конкретний курс та прогрес у ньому")
    public ResponseEntity<?> getCourseById(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");

        try {
            String email = principal.getAttribute("email");
            CourseProgressDto course = courseService.getCourseById(id, email);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}