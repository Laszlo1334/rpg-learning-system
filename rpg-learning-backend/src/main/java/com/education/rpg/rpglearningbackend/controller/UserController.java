package com.education.rpg.rpglearningbackend.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Гравці (Users)", description = "Методи для роботи з профілями користувачів")
public class UserController {

    @JsonIgnore
    private final UserService userService;
    private final UserRepository userRepository; // Додали для прямого читання статистики

    @GetMapping("/me")
    @Operation(summary = "Отримати мій профіль", description = "Повертає ігрові характеристики поточного гравця з перерахунком Енергії та Багаття.")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Увійдіть в систему!");
        }

        String email = principal.getAttribute("email");

        try {
            // Викликаємо сервіс, який оновить час, багаття та енергію, а потім поверне юзера
            User updatedUser = userService.getUserProfileByEmail(email);
            return ResponseEntity.ok(updatedUser);
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
}