package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.RunCompletionRequest;
import com.education.rpg.rpglearningbackend.model.CompletedTask;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.CompletedTaskRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CompletedTaskRepository completedTaskRepository;

    @Transactional
    public void processRunCompletion(String studentEmail, RunCompletionRequest request) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Студента не знайдено"));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Завдання не знайдено"));

        boolean hasApproved = completedTaskRepository.existsByTaskIdAndUserId(task.getId(), student.getId());
        LocalDateTime now = LocalDateTime.now();

        if (request.isVictory()) {
            if (!hasApproved) {
                grantRewards(student, task, now);
                completedTaskRepository.save(new CompletedTask(student, task));

                LocalDate today = LocalDate.now();
                LocalDate lastLogin = student.getLastLoginDate() != null ? student.getLastLoginDate().toLocalDate() : null;
                if (lastLogin == null || lastLogin.isBefore(today)) {
                    student.setCampfireLevel(Math.min(student.getCampfireLevel() + 1, 5));
                }
            }

            // Знімаємо щит, якщо він був використаний для безпечного проходження
            if (student.getHasActiveShield() != null && student.getHasActiveShield()) {
                student.setHasActiveShield(false);
            }

            student.setLastLoginDate(now);
        } else {
            // Game Over
            if (student.getHasActiveShield() != null && student.getHasActiveShield()) {
                log.info("Щит поглинув Game Over гравця {} у завданні {}", student.getEmail(), task.getId());
                student.setHasActiveShield(false); // Щит згорає, але помилка не йде в статистику
            } else if (request.getFailedQuestionIds() != null && !request.getFailedQuestionIds().isEmpty()) {
                handleProductiveFailure(student, request.getFailedQuestionIds());
            }
        }

        userRepository.save(student);
    }

    // --- ТВОЇ ЗБЕРЕЖЕНІ ПРИВАТНІ МЕТОДИ ---

    private void grantRewards(User student, Task task, LocalDateTime now) {
        // Базова енергія дає x1.5
        double energyMultiplier = (student.getEnergy() != null && student.getEnergy() > 0) ? 1.5 : 1.0;

        // Перевіряємо активні бафи від зілля
        boolean hasXpBuff = student.getXpBuffEndsAt() != null && now.isBefore(student.getXpBuffEndsAt());
        boolean hasGoldBuff = student.getGoldBuffEndsAt() != null && now.isBefore(student.getGoldBuffEndsAt());
        boolean hasEnergyStasis = student.getEnergyStasisEndsAt() != null && now.isBefore(student.getEnergyStasisEndsAt());

        // Застосовуємо бафи (наприклад, ще +50% якщо випив Еліксир)
        double finalXpMultiplier = hasXpBuff ? energyMultiplier + 0.5 : energyMultiplier;
        double finalGoldMultiplier = hasGoldBuff ? energyMultiplier + 1.0 : energyMultiplier; // Подвійне золото

        int finalXp = (int) (task.getRewardXp() * finalXpMultiplier);
        int finalGold = (int) (task.getRewardGold() * finalGoldMultiplier);

        student.setCurrentXp(student.getCurrentXp() + finalXp);
        student.setGold(student.getGold() + finalGold);
        student.setLifetimeGold(student.getLifetimeGold() + finalGold);
        student.setTotalTasksCompleted(student.getTotalTasksCompleted() + 1);

        // Якщо немає стазису кави — знімаємо енергію
        if (student.getEnergy() != null && !hasEnergyStasis) {
            student.setEnergy(Math.max(0, student.getEnergy() - 20));
        }

        student.setLastTaskCompletionDate(now);

        int calculatedLevel = (student.getCurrentXp() / 1000) + 1;
        if (calculatedLevel > student.getLevel()) {
            student.setLevel(calculatedLevel);
        }
    }

    private void handleProductiveFailure(User student, List<Long> failedQuestionIds) {
        // Використовуємо Set, щоб уникнути нарахування кристалів за ту саму помилку кілька разів у межах одного забігу
        Set<Long> uniqueFails = new HashSet<>(failedQuestionIds);
        int crystalReward = uniqueFails.size() * 5;

        student.setCrystals(student.getCrystals() + crystalReward);
        student.setLifetimeCrystals(student.getLifetimeCrystals() + crystalReward);
        student.setTotalFailures(student.getTotalFailures() + uniqueFails.size());
    }
}