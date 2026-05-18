package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.LeaderboardDto;
import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    // Метод для отримання таблиці лідерів
    public List<LeaderboardDto> getLeaderboard() {
        // Звертаємося до нашого оновленого методу, який враховує Приватність (opt-out)
        List<User> topStudents = userRepository.findTop10ByRoleAndIsPublicProfileTrueOrderByCurrentXpDesc(Role.STUDENT);

        return topStudents.stream().map(student -> {
            LeaderboardDto dto = new LeaderboardDto();
            dto.setId(student.getId());
            dto.setUsername(student.getUsername());
            dto.setLevel(student.getLevel());

            // ВИПРАВЛЕНО: Конвертуємо Integer у Long за допомогою .longValue()
            dto.setXp(student.getCurrentXp().longValue());

            // dto.setAvatarUrl(student.getAvatarUrl()); // Розікоментуй, якщо в User є поле avatarUrl
            return dto;
        }).collect(Collectors.toList());
    }

    // Отримання профілю з динамічним перерахунком ігрових метрик
    @Transactional // Обов'язково, щоб зберегти нові значення енергії в БД
    public User getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        LocalDateTime now = LocalDateTime.now();

        activityLogService.recordLogin(user);

        // 1. ЛОГІКА БАГАТТЯ: Зменшуємо рівень, якщо минуло більше 48 годин з останнього входу
        if (user.getLastLoginDate() != null) {
            long hoursSinceLastLogin = ChronoUnit.HOURS.between(user.getLastLoginDate(), now);
            if (hoursSinceLastLogin >= 48) {
                // Згасання на 1 рівень, але не нижче 1
                int newCampfireLevel = Math.max(1, user.getCampfireLevel() - 1);
                user.setCampfireLevel(newCampfireLevel);
            }
        }
        // Оновлюємо час останнього логіну/активності
        user.setLastLoginDate(now);

        // 2. ЛОГІКА ЕНЕРГІЇ: +1 одиниця за кожні 6 хвилин простою
        if (user.getLastTaskCompletionDate() != null && user.getEnergy() < 100) {
            long minutesPassed = Duration.between(user.getLastTaskCompletionDate(), now).toMinutes();
            int energyToAdd = (int) (minutesPassed / 6);

            if (energyToAdd > 0) {
                user.setEnergy(Math.min(100, user.getEnergy() + energyToAdd));
                // Зсуваємо час, щоб не втратити залишок хвилин
                user.setLastTaskCompletionDate(user.getLastTaskCompletionDate().plusMinutes(energyToAdd * 6));
            }
        }

        // Зберігаємо оновленого юзера і повертаємо його
        return userRepository.save(user);
    }
}