package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.AnswerResponse;
import com.education.rpg.rpglearningbackend.dto.RunCompletionRequest;
import com.education.rpg.rpglearningbackend.model.ActionType;
import com.education.rpg.rpglearningbackend.model.CompletedTask;
import com.education.rpg.rpglearningbackend.model.Question;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.model.UserQuestionFailure;
import com.education.rpg.rpglearningbackend.repository.CompletedTaskRepository;
import com.education.rpg.rpglearningbackend.repository.QuestionRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserQuestionFailureRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CompletedTaskRepository completedTaskRepository;
    private final QuestionRepository questionRepository;
    private final UserQuestionFailureRepository userQuestionFailureRepository;
    private final ActivityLogService activityLogService;

    private static final int CRYSTALS_PER_FIRST_FAILURE = 5;

    @Transactional
    public AnswerResponse checkAnswerAndProcessFailure(Long questionId, String userAnswer, String userEmail) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

        String normalizedUserAnswer = userAnswer.trim().replaceAll("\\s+", " ").toLowerCase();
        boolean isCorrect = question.getCorrectAnswers().stream()
                .map(ans -> ans.trim().replaceAll("\\s+", " ").toLowerCase())
                .anyMatch(ans -> ans.equals(normalizedUserAnswer));

        if (isCorrect) {
            return AnswerResponse.builder()
                    .isCorrect(true)
                    .explanation(null)
                    .crystalsAwarded(0)
                    .build();
        }

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return AnswerResponse.builder()
                    .isCorrect(false)
                    .explanation(question.getExplanation())
                    .crystalsAwarded(0)
                    .build();
        }

        User student = userOpt.get();
        Optional<UserQuestionFailure> existingFailure = userQuestionFailureRepository
                .findByUserIdAndQuestionId(student.getId(), question.getId());

        if (existingFailure.isPresent()) {
            UserQuestionFailure failure = existingFailure.get();
            failure.setFailureCount(failure.getFailureCount() + 1);
            failure.setLastWrongAnswer(userAnswer.trim());
            failure.setFailedAt(LocalDateTime.now());
            userQuestionFailureRepository.save(failure);

            return AnswerResponse.builder()
                    .isCorrect(false)
                    .explanation(question.getExplanation())
                    .crystalsAwarded(0)
                    .build();
        }

        userQuestionFailureRepository.save(
                UserQuestionFailure.builder()
                        .user(student)
                        .question(question)
                        .failureCount(1)
                        .lastWrongAnswer(userAnswer.trim())
                        .build()
        );
        student.setCrystals(student.getCrystals() + CRYSTALS_PER_FIRST_FAILURE);
        student.setLifetimeCrystals(student.getLifetimeCrystals() + CRYSTALS_PER_FIRST_FAILURE);
        userRepository.save(student);

        return AnswerResponse.builder()
                .isCorrect(false)
                .explanation(question.getExplanation())
                .crystalsAwarded(CRYSTALS_PER_FIRST_FAILURE)
                .build();
    }

    @Transactional
    public void processRunCompletion(String studentEmail, RunCompletionRequest request) {
        log.info("Run completion for {}, task: {}, victory: {}", studentEmail, request.getTaskId(), request.isVictory());

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        boolean alreadyCompleted = completedTaskRepository.existsByTaskIdAndUserId(task.getId(), student.getId());
        LocalDateTime now = LocalDateTime.now();

        if (request.isVictory()) {
            if (!alreadyCompleted) {
                boolean flawless = isFlawlessAttempt(request);
                int levelBefore = student.getLevel();

                RewardOutcome outcome = grantRewards(student, task, now, flawless);

                CompletedTask completedTask = buildCompletedTask(student, task, request);
                completedTaskRepository.save(completedTask);

                long timeSpent = completedTask.getTimeSpentSeconds() != null ? completedTask.getTimeSpentSeconds() : 0L;
                student.setTotalPlayTimeSeconds(student.getTotalPlayTimeSeconds() + timeSpent);

                activityLogService.log(student, ActionType.TASK_COMPLETED, buildTaskCompletedDetails(
                        task.getId(), outcome, flawless));

                if (student.getLevel() > levelBefore) {
                    activityLogService.log(student, ActionType.LEVEL_UP,
                            String.format("{\"level\":%d,\"previousLevel\":%d}", student.getLevel(), levelBefore));
                }

                LocalDate today = LocalDate.now();
                LocalDate lastLogin = student.getLastLoginDate() != null
                        ? student.getLastLoginDate().toLocalDate()
                        : null;
                if (lastLogin == null || lastLogin.isBefore(today)) {
                    student.setCampfireLevel(Math.min(student.getCampfireLevel() + 1, 5));
                }
            }

            if (Boolean.TRUE.equals(student.getHasActiveShield())) {
                student.setHasActiveShield(false);
            }

            student.setLastLoginDate(now);
            userRepository.save(student);
        } else {
            student.setCurrentFlawlessStreak(0);

            if (Boolean.TRUE.equals(student.getHasActiveShield())) {
                log.info("Shield absorbed defeat for {} on task {}", student.getEmail(), task.getId());
                student.setHasActiveShield(false);
            } else if (request.getFailedQuestionIds() != null && !request.getFailedQuestionIds().isEmpty()) {
                handleProductiveFailure(student, request.getFailedQuestionIds());
                activityLogService.log(student, ActionType.BOSS_FAILED,
                        String.format("{\"taskId\":%d,\"failedQuestions\":%d}",
                                task.getId(), new HashSet<>(request.getFailedQuestionIds()).size()));
            }

            userRepository.save(student);
        }
    }

   public boolean isFlawlessAttempt(RunCompletionRequest request) {
    if (request.getFailedQuestionIds() != null) {
        return request.getFailedQuestionIds().isEmpty();
    }
    return false;
}

    private CompletedTask buildCompletedTask(User student, Task task, RunCompletionRequest request) {
        CompletedTask completedTask = new CompletedTask(student, task);
        completedTask.setAttemptsTaken(request.getAttemptsTaken() != null ? request.getAttemptsTaken() : 1);
        completedTask.setHintsUsed(Boolean.TRUE.equals(request.getHintsUsed()));
        completedTask.setTimeSpentSeconds(request.getTimeSpentSeconds() != null ? request.getTimeSpentSeconds() : 0L);
        return completedTask;
    }

    private RewardOutcome grantRewards(User student, Task task, LocalDateTime now, boolean flawless) {
        int baseXp = task.getRewardXp() != null ? task.getRewardXp() : 0;
        int baseGold = task.getRewardGold() != null ? task.getRewardGold() : 0;

        double multiplier = 1.0;
        if (flawless) {
            int newStreak = student.getCurrentFlawlessStreak() + 1;
            student.setCurrentFlawlessStreak(newStreak);
            if (newStreak > student.getLongestFlawlessStreak()) {
                student.setLongestFlawlessStreak(newStreak);
            }
            multiplier = 1.0 + Math.min(student.getCurrentFlawlessStreak() * 0.1, 1.0);
        } else {
            student.setCurrentFlawlessStreak(0);
        }

        int finalXp = (int) Math.round(baseXp * multiplier);
        int finalGold = (int) Math.round(baseGold * multiplier);

        if (student.getXpBuffEndsAt() != null && student.getXpBuffEndsAt().isAfter(now)) {
            finalXp = (int) (finalXp * 1.5);
            log.info("XP buff applied after flawless multiplier. Result: {}", finalXp);
        }

        if (student.getGoldBuffEndsAt() != null && student.getGoldBuffEndsAt().isAfter(now)) {
            finalGold = finalGold * 2;
            log.info("Gold buff applied after flawless multiplier. Result: {}", finalGold);
        }

        student.setCurrentXp(student.getCurrentXp() + finalXp);
        student.setGold(student.getGold() + finalGold);
        student.setLifetimeGold(student.getLifetimeGold() + finalGold);
        student.setTotalTasksCompleted(student.getTotalTasksCompleted() + 1);
        student.setLastTaskCompletionDate(now);

        return new RewardOutcome(baseXp, baseGold, finalXp, finalGold, multiplier, flawless);
    }

    private String buildTaskCompletedDetails(Long taskId, RewardOutcome outcome, boolean flawless) {
        return String.format(
                "{\"taskId\":%d,\"flawless\":%s,\"multiplier\":%.2f,\"baseXp\":%d,\"finalXp\":%d,\"baseGold\":%d,\"finalGold\":%d}",
                taskId, flawless, outcome.multiplier(), outcome.baseXp(), outcome.finalXp(),
                outcome.baseGold(), outcome.finalGold());
    }

    private void handleProductiveFailure(User student, List<Long> failedQuestionIds) {
        Set<Long> uniqueFails = new HashSet<>(failedQuestionIds);
        student.setTotalFailures(student.getTotalFailures() + uniqueFails.size());
    }

    private record RewardOutcome(int baseXp, int baseGold, int finalXp, int finalGold, double multiplier, boolean flawless) {}
}
