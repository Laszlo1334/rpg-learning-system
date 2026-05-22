package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.QuestionDto;
import com.education.rpg.rpglearningbackend.dto.TaskDto;
import com.education.rpg.rpglearningbackend.model.CompletedTask;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.CompletedTaskRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.education.rpg.rpglearningbackend.model.Question;
import java.util.ArrayList;
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

    // Safely fetch a single task for the Arena, enforcing lock rules
    public TaskDto getTaskById(Long taskId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        boolean isCompleted = completedTaskRepository.existsByTaskIdAndUserId(taskId, user.getId());

        // Enforce prerequisite lock: incomplete tasks require all prereqs to be done
        if (!isCompleted) {
            List<Long> prereqs = task.getPrerequisiteTaskIds();
            if (prereqs != null && !prereqs.isEmpty()) {
                List<Long> completedTaskIds = completedTaskRepository.findByUserId(user.getId())
                        .stream()
                        .map(ct -> ct.getTask().getId())
                        .collect(Collectors.toList());

                boolean allPrereqsMet = completedTaskIds.containsAll(prereqs);
                if (!allPrereqsMet) {
                    throw new RuntimeException("This task is locked! Complete the prerequisite quests first.");
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
                .orElseThrow(() -> new RuntimeException("User not found"));

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
        if (task.getCourse() != null) {
            dto.setCourseId(task.getCourse().getId());
        }
        dto.setTitle(task.getTitle());
        dto.setTheoryContent(task.getTheoryContent());
        dto.setBranchName(task.getBranchName());
        dto.setOrderIndex(task.getOrderIndex());
        dto.setIsTheoryHidden(task.getIsTheoryHidden());
        dto.setRewardXp(task.getRewardXp());
        dto.setRewardGold(task.getRewardGold());
        dto.setPrerequisiteTaskIds(task.getPrerequisiteTaskIds());
        dto.setDynamicQuestionCount(task.getDynamicQuestionCount());
        dto.setType(task.getType().name());

        if (task.getType() == Task.TaskType.BOSS) {
            TaskDto.BossMetadata meta = new TaskDto.BossMetadata();
            meta.setBossName(task.getBossName());
            meta.setBossAvatar(task.getBossAvatarUrl());
            meta.setTimeLimitSeconds(task.getTimeLimitSeconds());
            dto.setBossMetadata(meta);
        }

        if (task.getQuestions() != null && !task.getQuestions().isEmpty()) {
            // Copy to avoid mutating Hibernate's cached entity collection
            List<Question> allQuestions = new ArrayList<>(task.getQuestions());


            int limit = task.getDynamicQuestionCount() != null ? task.getDynamicQuestionCount() : allQuestions.size();


            java.util.Collections.shuffle(allQuestions);

            // Shuffle, slice to dynamicQuestionCount, and map to a DTO that omits correct answers
            List<QuestionDto> safeQuestions = allQuestions.stream()
                    .limit(limit)
                    .map(q -> {
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

}