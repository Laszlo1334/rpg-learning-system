package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.UserDto;
import com.education.rpg.rpglearningbackend.dto.UserStatsDto;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import com.education.rpg.rpglearningbackend.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Гравці (Users)", description = "Методи для роботи з профілями користувачів")
public class UserController {

    @JsonIgnore
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Отримати мій профіль", description = "Повертає ігрові характеристики поточного гравця з перерахунком Енергії та Багаття.")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Увійдіть в систему!");
        }

        String email = principal.getAttribute("email");

        try {
            User user = userService.getUserProfileByEmail(email);
            UserDto dto = UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .avatarUrl(user.getAvatarUrl())
                    .level(user.getLevel())
                    .currentXp(user.getCurrentXp())
                    .gold(user.getGold())
                    .crystals(user.getCrystals())
                    .campfireLevel(user.getCampfireLevel())
                    .lastLoginDate(user.getLastLoginDate())
                    .energy(user.getEnergy())
                    .lastTaskCompletionDate(user.getLastTaskCompletionDate())
                    .isPublicProfile(user.getIsPublicProfile())
                    .xpBuffEndsAt(user.getXpBuffEndsAt())
                    .goldBuffEndsAt(user.getGoldBuffEndsAt())
                    .energyStasisEndsAt(user.getEnergyStasisEndsAt())
                    .hasActiveShield(user.getHasActiveShield())
                    .lifetimeGold(user.getLifetimeGold())
                    .lifetimeCrystals(user.getLifetimeCrystals())
                    .totalTasksCompleted(user.getTotalTasksCompleted())
                    .totalFailures(user.getTotalFailures())
                    .totalLoginDays(user.getTotalLoginDays())
                    .longestLoginStreak(user.getLongestLoginStreak())
                    .totalPlayTimeSeconds(user.getTotalPlayTimeSeconds())
                    .currentFlawlessStreak(user.getCurrentFlawlessStreak())
                    .longestFlawlessStreak(user.getLongestFlawlessStreak())
                    .build();
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/me/stats")
    @Operation(summary = "Особиста Справа", description = "Повертає накопичувальну макро-статистику для модального вікна (без важких перерахунків)")
    public ResponseEntity<UserStatsDto> getMyStats(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        UserStatsDto stats = UserStatsDto.builder()
                .lifetimeGold(user.getLifetimeGold())
                .lifetimeCrystals(user.getLifetimeCrystals())
                .totalTasksCompleted(user.getTotalTasksCompleted())
                .totalFailures(user.getTotalFailures())
                .build();

        return ResponseEntity.ok(stats);
    }

    @PutMapping("/me/privacy")
    @Operation(summary = "Update profile privacy", description = "Toggles the player's participation in the leaderboard (opt-out system)")
    public ResponseEntity<?> updatePrivacy(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam boolean isPublic) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Please log in!");
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        user.setIsPublicProfile(isPublic);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/consume-shield")
    @Operation(summary = "Burn active shield", description = "Consumes the Rune of Protection shield after it absorbs one defeat")
    public ResponseEntity<?> consumeShield(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Please log in!");
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (!Boolean.TRUE.equals(user.getHasActiveShield())) {
            return ResponseEntity.badRequest().body("No active shield to consume.");
        }

        user.setHasActiveShield(false);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
