package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.AnswerResponse;
import com.education.rpg.rpglearningbackend.dto.RunCompletionRequest;
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

    private static final int CRYSTALS_PER_FIRST_FAILURE = 5;

    @Transactional
    public AnswerResponse checkAnswerAndProcessFailure(Long questionId, String userAnswer, String userEmail) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Запитання не знайдено: " + questionId));

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

        // Відповідь НЕПРАВИЛЬНА — намагаємось нарахувати кристали
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            // Гість або неавторизований — повертаємо без кристалів
            return AnswerResponse.builder()
                    .isCorrect(false)
                    .explanation(question.getExplanation())
                    .crystalsAwarded(0)
                    .build();
        }

        User student = userOpt.get();
        boolean alreadyFailed = userQuestionFailureRepository
                .existsByUserIdAndQuestionId(student.getId(), question.getId());

        if (!alreadyFailed) {
            // Перша помилка на цьому питанні — нараховуємо кристали
            userQuestionFailureRepository.save(
                    UserQuestionFailure.builder()
                            .user(student)
                            .question(question)
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
        } else {
            // Повторна помилка — кристали не нараховуються
            return AnswerResponse.builder()
                    .isCorrect(false)
                    .explanation(question.getExplanation())
                    .crystalsAwarded(0)
                    .build();
        }
    }

    @Transactional
    public void processRunCompletion(String studentEmail, RunCompletionRequest request) {
        log.info("Завершення забігу для {}, завдання: {}, перемога: {}", studentEmail, request.getTaskId(), request.isVictory());

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Студента не знайдено"));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Завдання не знайдено"));

        boolean hasApproved = completedTaskRepository.existsByTaskIdAndUserId(task.getId(), student.getId());
        log.info("Чи проходив юзер це раніше? {}", hasApproved);
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
            log.info("Нараховано нагороди. Новий XP: {}, Золото: {}", student.getCurrentXp(), student.getGold());

            // КРИТИЧНО: Зберігаємо юзера після блоку перемоги
            userRepository.save(student);
        } else {
            // Game Over
            if (student.getHasActiveShield() != null && student.getHasActiveShield()) {
                log.info("Щит поглинув Game Over гравця {} у завданні {}", student.getEmail(), task.getId());
                student.setHasActiveShield(false); // Щит згорає, але помилка не йде в статистику
            } else if (request.getFailedQuestionIds() != null && !request.getFailedQuestionIds().isEmpty()) {
                handleProductiveFailure(student, request.getFailedQuestionIds());
            }

            userRepository.save(student);
        }
    }

    // --- ТВОЇ ЗБЕРЕЖЕНІ ПРИВАТНІ МЕТОДИ ---

    private void grantRewards(User student, Task task, LocalDateTime now) {
        int finalXp = task.getRewardXp();
        int finalGold = task.getRewardGold();

        // Застосування Зілля Досвіду (x1.5)
        if (student.getXpBuffEndsAt() != null && student.getXpBuffEndsAt().isAfter(now)) {
            finalXp = (int) (finalXp * 1.5);
            log.info("XP Buff applied! Original: {}, New: {}", task.getRewardXp(), finalXp);
        }

        // Застосування Зілля Золота (x2)
        if (student.getGoldBuffEndsAt() != null && student.getGoldBuffEndsAt().isAfter(now)) {
            finalGold = finalGold * 2;
            log.info("Gold Buff applied! Original: {}, New: {}", task.getRewardGold(), finalGold);
        }

        student.setCurrentXp(student.getCurrentXp() + finalXp);
        student.setGold(student.getGold() + finalGold);
        student.setTotalTasksCompleted(student.getTotalTasksCompleted() + 1);
        student.setLastTaskCompletionDate(now);
    }

    private void handleProductiveFailure(User student, List<Long> failedQuestionIds) {
        // Кристали вже були нараховані в реальному часі через checkAnswerAndProcessFailure.
        // Тут лише фіксуємо загальну кількість помилок для статистики.
        Set<Long> uniqueFails = new HashSet<>(failedQuestionIds);
        student.setTotalFailures(student.getTotalFailures() + uniqueFails.size());
    }
}