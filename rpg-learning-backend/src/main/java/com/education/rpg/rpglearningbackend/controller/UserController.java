package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Гравці (Users)", description = "Методи для роботи з профілями користувачів")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
}