package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.SubmissionRequest;
import com.education.rpg.rpglearningbackend.dto.SubmissionReviewRequest;
import com.education.rpg.rpglearningbackend.model.*;
import com.education.rpg.rpglearningbackend.repository.SubmissionRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional // Гарантує, що у разі помилки дані не збережуться наполовину
    public Submission processSubmission(String studentEmail, SubmissionRequest request) {

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Студента не знайдено"));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Завдання не знайдено"));

        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setTask(task);
        submission.setStudentAnswer(request.getAnswer());
        submission.setAttemptNumber(1); // Для початку ставимо 1 спробу

        if (task.getVerificationType() == VerificationType.MANUAL) {
            submission.setStatus(SubmissionStatus.PENDING);
            log.info("Квест {} відправлено на ручну перевірку студентом {}", task.getId(), studentEmail);
        }
        else if (task.getVerificationType() == VerificationType.AUTO) {
            // Перевіряємо відповідь (ігноруючи регістр та зайві пробіли)
            boolean isCorrect = task.getCorrectAnswer() != null &&
                    task.getCorrectAnswer().trim().equalsIgnoreCase(request.getAnswer().trim());

            if (isCorrect) {
                submission.setStatus(SubmissionStatus.APPROVED);
                grantRewards(student, task);
                log.info("Квест {} успішно пройдено студентом {}!", task.getId(), studentEmail);
            } else {
                submission.setStatus(SubmissionStatus.REJECTED);
                log.info("Студент {} дав неправильну відповідь на квест {}", studentEmail, task.getId());
            }
        }

        userRepository.save(student);
        return submissionRepository.save(submission);
    }

    // НОВИЙ МЕТОД: Перевірка завдання вчителем
    @Transactional
    public Submission reviewSubmission(Long submissionId, String reviewerEmail, SubmissionReviewRequest request) {
        // 1. Знаходимо користувача, який робить запит (Вчителя)
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        // 2. Перевіряємо, чи має він права вчителя (або адміна)
        if (reviewer.getRole() != Role.TEACHER && reviewer.getRole() != Role.ADMIN) {
            throw new RuntimeException("У вас немає прав для перевірки завдань!");
        }

        // 3. Знаходимо саму відповідь студента
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Відповідь не знайдено"));

        // 4. Перевіряємо, щоб не нарахувати нагороду двічі
        if (submission.getStatus() == SubmissionStatus.APPROVED) {
            throw new RuntimeException("Це завдання вже було перевірено та зараховано!");
        }

        // 5. Оновлюємо статус та коментар
        submission.setStatus(request.getStatus());
        submission.setTeacherComment(request.getTeacherComment());

        // 6. Якщо вчитель схвалив завдання — видаємо нагороду студенту
        if (request.getStatus() == SubmissionStatus.APPROVED) {
            User student = submission.getStudent();
            Task task = submission.getTask();

            grantRewards(student, task); // Нараховуємо XP та монети
            userRepository.save(student); // Зберігаємо оновленого студента

            log.info("Вчитель {} схвалив завдання {} для студента {}", reviewerEmail, task.getId(), student.getEmail());
        } else {
            log.info("Вчитель {} відхилив завдання {} для студента {}", reviewerEmail, submission.getTask().getId(), submission.getStudent().getEmail());
        }

        return submissionRepository.save(submission);
    }

    private void grantRewards(User student, Task task) {
        // Додаємо XP та монети (конвертуємо Integer з Task у Long для User)
        student.setXp(student.getXp() + task.getRewardXp().longValue());
        student.setCoins(student.getCoins() + task.getRewardCoins().longValue());

        // Формула рівня: 1 рівень за кожні 100 XP
        int calculatedLevel = (int) (student.getXp() / 100) + 1;

        if (calculatedLevel > student.getLevel()) {
            student.setLevel(calculatedLevel);
            log.info("✨ Студент {} отримав новий рівень: {}!", student.getEmail(), student.getLevel());
        }
    }
}