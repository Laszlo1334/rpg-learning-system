package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.CourseProgressDto;
import com.education.rpg.rpglearningbackend.model.Course;
import com.education.rpg.rpglearningbackend.model.Submission;
import com.education.rpg.rpglearningbackend.model.SubmissionStatus;
import com.education.rpg.rpglearningbackend.model.Task;
import com.education.rpg.rpglearningbackend.model.User;
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
        List<Task> courseTasks = taskRepository.findByCourseId(course.getId());
        int totalTasks = courseTasks.size();
        dto.setTotalTasks(totalTasks);

        // 2. Рахуємо пройдені завдання (APPROVED)
        // Для MVP ми просто витягуємо всі успішні сабмішени студента і фільтруємо по курсу
        List<Submission> approvedSubmissions = submissionRepository.findAll().stream()
                .filter(sub -> sub.getStudent().getId().equals(student.getId()))
                .filter(sub -> sub.getStatus() == SubmissionStatus.APPROVED)
                .filter(sub -> sub.getTask().getCourse() != null && sub.getTask().getCourse().getId().equals(course.getId()))
                .toList();

        // Беремо унікальні завдання (бо студент міг здати одне завдання кілька разів, хоча ми це блокуємо)
        long completedTasks = approvedSubmissions.stream()
                .map(sub -> sub.getTask().getId())
                .distinct()
                .count();

        dto.setCompletedTasks((int) completedTasks);

        // 3. Вираховуємо відсоток (захист від ділення на 0)
        if (totalTasks == 0) {
            dto.setProgressPercentage(0);
        } else {
            dto.setProgressPercentage((int) ((completedTasks * 100) / totalTasks));
        }

        return dto;
    }
}