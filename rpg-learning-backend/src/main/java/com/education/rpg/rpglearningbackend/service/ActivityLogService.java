package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.model.ActionType;
import com.education.rpg.rpglearningbackend.model.ActivityLog;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final Pattern LOGIN_STREAK_PATTERN = Pattern.compile("\"loginStreak\":(\\d+)");

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(User user, ActionType actionType, String details) {
        activityLogRepository.save(ActivityLog.builder()
                .user(user)
                .actionType(actionType)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * Records a unique login day and updates login streak metrics.
     * Uses the user's lastLoginDate before it is overwritten by profile refresh.
     */
    @Transactional
    public void recordLogin(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate lastLoginDay = user.getLastLoginDate() != null
                ? user.getLastLoginDate().toLocalDate()
                : null;

        if (lastLoginDay != null && lastLoginDay.equals(today)) {
            return;
        }

        int loginStreak = 1;
        if (lastLoginDay != null && lastLoginDay.equals(today.minusDays(1))) {
            loginStreak = resolveLastLoginStreak(user.getId()) + 1;
        }

        user.setTotalLoginDays(user.getTotalLoginDays() + 1);
        if (loginStreak > user.getLongestLoginStreak()) {
            user.setLongestLoginStreak(loginStreak);
        }

        log(user, ActionType.LOGIN, String.format("{\"loginStreak\":%d}", loginStreak));
    }

    private int resolveLastLoginStreak(Long userId) {
        Optional<ActivityLog> lastLogin = activityLogRepository
                .findTopByUserIdAndActionTypeOrderByTimestampDesc(userId, ActionType.LOGIN);
        if (lastLogin.isEmpty() || lastLogin.get().getDetails() == null) {
            return 1;
        }
        Matcher matcher = LOGIN_STREAK_PATTERN.matcher(lastLogin.get().getDetails());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }
}
