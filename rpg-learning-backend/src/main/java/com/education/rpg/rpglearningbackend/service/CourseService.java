package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.CourseProgressDto;
import com.education.rpg.rpglearningbackend.model.Course;
import com.education.rpg.rpglearningbackend.model.Submission;
import com.education.rpg.rpglearningbackend.model.SubmissionStatus;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.CompletedTaskRepository;
import com.education.rpg.rpglearningbackend.repository.CourseRepository;
import com.education.rpg.rpglearningbackend.repository.SubmissionRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CompletedTaskRepository completedTaskRepository;

    // Отримати всі курси разом із прогресом для Глобальної Карти
    public List<CourseProgressDto> getAllCoursesWithProgress(String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        List<Course> allCourses = courseRepository.findAll();

        return allCourses.stream().map(course -> buildCourseProgress(course, student)).collect(Collectors.toList());
    }

    // Отримати один курс (для входу в Підземелля)
    public CourseProgressDto getCourseById(Long courseId, String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не знайдено"));

        return buildCourseProgress(course, student);
    }

    // --- ПРИВАТНИЙ МЕТОД: Розрахунок математики прогресу ---
    private CourseProgressDto buildCourseProgress(Course course, User student) {
        CourseProgressDto dto = new CourseProgressDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());

        // 1. Рахуємо всі завдання в цьому курсі
        int totalTasks = taskRepository.countByCourseId(course.getId());
        dto.setTotalTasks(totalTasks);

        // 2. Рахуємо пройдені завдання (APPROVED)
        int completedTasks = completedTaskRepository.countByUserIdAndTaskCourseId(student.getId(), course.getId());
        dto.setCompletedTasks(completedTasks);

        // 3. Вираховуємо відсоток (захист від ділення на 0)
        if (totalTasks == 0) {
            dto.setProgressPercentage(0);
        } else {
            dto.setProgressPercentage((completedTasks * 100) / totalTasks);
        }
        
        // 4. Логіка статусів
        String status = "new";
        if (completedTasks == totalTasks && totalTasks > 0) {
            status = "completed";
        } else if (completedTasks > 0) {
            status = "in_progress";
        }
        dto.setStatus(status);

        return dto;
    }
}