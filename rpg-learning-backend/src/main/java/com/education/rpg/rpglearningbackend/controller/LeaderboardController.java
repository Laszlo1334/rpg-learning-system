package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.CourseLeaderboardDto;
import com.education.rpg.rpglearningbackend.dto.LeaderboardDto;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import com.education.rpg.rpglearningbackend.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Hall of Fame (Leaderboard)", description = "Global and per-course player rankings")
public class LeaderboardController {

    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    @Operation(summary = "Global Leaderboard", description = "Top 10 players by total XP")
    public ResponseEntity<List<LeaderboardDto>> getGlobalLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard());
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Course Leaderboard", description = "Top players within a specific course (respects privacy settings)")
    public ResponseEntity<List<CourseLeaderboardDto>> getCourseLeaderboard(@PathVariable Long courseId) {
        // Delegate to the service so that avatar URLs are resolved from the inventory
        return ResponseEntity.ok(leaderboardService.getCourseLeaderboard(courseId));
    }
}