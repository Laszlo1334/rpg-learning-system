package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.CourseLeaderboardDto;
import com.education.rpg.rpglearningbackend.dto.LeaderboardDto;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
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
@Tag(name = "Зал Слави (Leaderboard)", description = "Глобальний та мікро-рейтинги гравців")
public class LeaderboardController {

    private final UserRepository userRepository;

    @GetMapping("/global")
    @Operation(summary = "Глобальний Лідерборд", description = "Топ-10 гравців за загальним XP")
    public ResponseEntity<List<LeaderboardDto>> getGlobalLeaderboard() {
        List<LeaderboardDto> topPlayers = userRepository.findTop10ByIsPublicProfileTrueOrderByCurrentXpDesc()
                .stream()
                .map(user -> {
                    LeaderboardDto dto = new LeaderboardDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setLevel(user.getLevel());
                    dto.setXp(Long.valueOf(user.getCurrentXp()));
                    dto.setAvatarUrl(user.getAvatarUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(topPlayers);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Мікро-Лідерборд", description = "Топ гравців у межах конкретного курсу (з урахуванням приватності)")
    public ResponseEntity<List<CourseLeaderboardDto>> getCourseLeaderboard(@PathVariable Long courseId) {
        // Завдяки @Query у репозиторії, цей метод одразу повертає готові DTO!
        return ResponseEntity.ok(userRepository.getLeaderboardByCourseId(courseId));
    }
}