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


    public List<LeaderboardDto> getLeaderboard() {

        List<User> topStudents = userRepository.findTop10ByRoleAndIsPublicProfileTrueOrderByCurrentXpDesc(Role.STUDENT);

        return topStudents.stream().map(student -> {
            LeaderboardDto dto = new LeaderboardDto();
            dto.setId(student.getId());
            dto.setUsername(student.getUsername());
            dto.setLevel(student.getLevel());


            dto.setXp(student.getCurrentXp().longValue());


            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public User getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        LocalDateTime now = LocalDateTime.now();

        activityLogService.recordLogin(user);

        // Campfire decay: reduce level by 1 (min 1) if the user has been absent 48+ hours
        if (user.getLastLoginDate() != null) {
            long hoursSinceLastLogin = ChronoUnit.HOURS.between(user.getLastLoginDate(), now);
            if (hoursSinceLastLogin >= 48) {

                int newCampfireLevel = Math.max(1, user.getCampfireLevel() - 1);
                user.setCampfireLevel(newCampfireLevel);
            }
        }

        user.setLastLoginDate(now);

        // Energy regenerates at +1 per 6 minutes of idle time, up to 100
        if (user.getLastTaskCompletionDate() != null && user.getEnergy() < 100) {
            long minutesPassed = Duration.between(user.getLastTaskCompletionDate(), now).toMinutes();
            int energyToAdd = (int) (minutesPassed / 6);

            if (energyToAdd > 0) {
                user.setEnergy(Math.min(100, user.getEnergy() + energyToAdd));
                // Advance the reference time to preserve sub-period remainder
                user.setLastTaskCompletionDate(user.getLastTaskCompletionDate().plusMinutes(energyToAdd * 6));
            }
        }


        return userRepository.save(user);
    }
}