package com.education.rpg.rpglearningbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

// exclude вимикає екран логіну, щоб нам було зручніше розробляти
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableScheduling
public class RpgLearningBackendApplication {

    public static void main(String[] args) {
        // 1. Встановлюємо UTC НАЙПЕРШИМ рядком, до запуску Spring
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // 2. Тепер запускаємо сервер
        SpringApplication.run(RpgLearningBackendApplication.class, args);
    }
}