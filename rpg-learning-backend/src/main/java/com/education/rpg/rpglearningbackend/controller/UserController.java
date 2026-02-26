package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.dto.LeaderboardDto;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import com.education.rpg.rpglearningbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // Автоматично створює конструктор для final полів
@Tag(name = "Гравці (Users)", description = "Методи для роботи з профілями користувачів")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService; // Додали наш новий сервіс

    @GetMapping("/me")
    @Operation(summary = "Отримати мій профіль", description = "Повертає ігрові характеристики (XP, рівень, монети) поточного авторизованого гравця.")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        // Якщо юзер не залогінений
        if (principal == null) {
            return ResponseEntity.status(401).body("Увійдіть в систему!");
        }

        // Дістаємо email з Google-акаунта
        String email = principal.getAttribute("email");

        // Шукаємо гравця в нашій RPG-базі
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(404).body("Гравця не знайдено!");
        }
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Отримати Топ-10 найкращих студентів (Leaderboard)", description = "Повертає рейтинг гравців, відсортований за кількістю XP.")
    public ResponseEntity<List<LeaderboardDto>> getLeaderboard() {
        // Звертаємося до сервісу, який повертає безпечні DTO без паролів та email
        return ResponseEntity.ok(userService.getLeaderboard());
    }
}