package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.CourseProgressDto;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.service.CourseService;
import com.education.rpg.rpglearningbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<CourseProgressDto>> getAllCourses(@AuthenticationPrincipal OAuth2User principal) {
        User user = userService.getUserProfileByEmail(principal.getAttribute("email"));
        return ResponseEntity.ok(courseService.getAllCoursesWithProgress(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseProgressDto> getCourse(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        User user = userService.getUserProfileByEmail(principal.getAttribute("email"));
        return ResponseEntity.ok(courseService.getCourseById(id, user));
    }
}