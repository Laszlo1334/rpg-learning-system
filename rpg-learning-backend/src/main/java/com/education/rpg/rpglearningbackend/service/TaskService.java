package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.QuestionDto;
import com.education.rpg.rpglearningbackend.dto.TaskDto;
import com.education.rpg.rpglearningbackend.model.CompletedTask;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.CompletedTaskRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CompletedTaskRepository completedTaskRepository;
    private final UserRepository userRepository;

    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // НОВЕ: Безпечне отримання ОДНОГО завдання для Арени
    public TaskDto getTaskById(Long taskId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Завдання не знайдено"));

        boolean isCompleted = completedTaskRepository.existsByTaskIdAndUserId(taskId, user.getId());

        // Якщо квест ще не пройдено, перевіряємо, чи не заблокований він
        if (!isCompleted) {
            List<Long> prereqs = task.getPrerequisiteTaskIds();
            if (prereqs != null && !prereqs.isEmpty()) {
                List<Long> completedTaskIds = completedTaskRepository.findByUserId(user.getId())
                        .stream()
                        .map(ct -> ct.getTask().getId())
                        .collect(Collectors.toList());

                boolean allPrereqsMet = completedTaskIds.containsAll(prereqs);
                if (!allPrereqsMet) {
                    throw new RuntimeException("Це завдання заблоковано! Пройдіть попередні квести.");
                }
            }
        }

        TaskDto dto = convertToDto(task);
        dto.setIsCompleted(isCompleted);
        dto.setIsLocked(false);
        return dto;
    }

    public List<TaskDto> getTasksByCourseId(Long courseId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        List<Task> tasks = taskRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        List<Long> completedTaskIds = completedTaskRepository.findByUserId(user.getId())
                .stream()
                .map(ct -> ct.getTask().getId())
                .collect(Collectors.toList());

        return tasks.stream().map(task -> {
            TaskDto dto = convertToDto(task);
            boolean isCompleted = completedTaskIds.contains(task.getId());
            dto.setIsCompleted(isCompleted);

            if (isCompleted) {
                dto.setIsLocked(false);
            } else {
                boolean allPrereqsMet = true;
                List<Long> prereqs = task.getPrerequisiteTaskIds();

                if (prereqs != null && !prereqs.isEmpty()) {
                    allPrereqsMet = completedTaskIds.containsAll(prereqs);
                }
                dto.setIsLocked(!allPrereqsMet);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setTheoryContent(task.getTheoryContent());
        dto.setBranchName(task.getBranchName());
        dto.setOrderIndex(task.getOrderIndex());
        dto.setIsTheoryHidden(task.getIsTheoryHidden());
        dto.setRewardXp(task.getRewardXp());
        dto.setRewardGold(task.getRewardGold());
        dto.setPrerequisiteTaskIds(task.getPrerequisiteTaskIds());

        if (task.getQuestions() != null) {
            List<QuestionDto> safeQuestions = task.getQuestions().stream().map(q -> {
                QuestionDto qDto = new QuestionDto();
                qDto.setId(q.getId());
                qDto.setQuestionText(q.getQuestionText());
                qDto.setType(q.getType());
                qDto.setOptions(q.getOptions());
                return qDto;
            }).collect(Collectors.toList());
            dto.setQuestions(safeQuestions);
        }

        return dto;
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public TaskDto getMemoryTask(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<CompletedTask> oldTasks = completedTaskRepository.findByUserIdAndCompletedAtBefore(user.getId(), threeDaysAgo);

        if (oldTasks.isEmpty()) {
            return null;
        }

        int randomIndex = new java.util.Random().nextInt(oldTasks.size());
        Task randomOldTask = oldTasks.get(randomIndex).getTask();

        return convertToDto(randomOldTask);
    }
}