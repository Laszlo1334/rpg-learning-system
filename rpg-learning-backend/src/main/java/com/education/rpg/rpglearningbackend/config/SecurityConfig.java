package com.education.rpg.rpglearningbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Вимикаємо CSRF, бо ми будемо використовувати REST API (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Налаштування доступів
                .authorizeHttpRequests(auth -> auth
                        // Дозволяємо всім доступ до реєстрації та логіну
                        .requestMatchers("/api/auth/**").permitAll()
                        // Все інше - тільки для авторизованих
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Найкращий стандарт для хешування паролів
    }
}