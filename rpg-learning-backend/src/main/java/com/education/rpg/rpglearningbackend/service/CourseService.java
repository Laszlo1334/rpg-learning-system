package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.CourseProgressDto;
import com.education.rpg.rpglearningbackend.model.Course;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.CompletedTaskRepository;
import com.education.rpg.rpglearningbackend.repository.CourseRepository;
import com.education.rpg.rpglearningbackend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final CompletedTaskRepository completedTaskRepository;

    public List<CourseProgressDto> getAllCoursesWithProgress(User user) {
        List<Course> courses = courseRepository.findAll();

        return courses.stream().map(course -> {
            int totalTasks = taskRepository.countByCourseId(course.getId());
            int completedTasks = completedTaskRepository.countByUserIdAndTaskCourseId(user.getId(), course.getId());

            String status = "new";
            if (totalTasks > 0 && completedTasks == totalTasks) {
                status = "completed";
            } else if (completedTasks > 0) {
                status = "in_progress";
            }

            return CourseProgressDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .status(status)
                .build();
        }).collect(Collectors.toList());
    }

    public CourseProgressDto getCourseById(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        int totalTasks = taskRepository.countByCourseId(course.getId());
        int completedTasks = completedTaskRepository.countByUserIdAndTaskCourseId(user.getId(), course.getId());

        String status = "new";
        if (totalTasks > 0 && completedTasks == totalTasks) {
            status = "completed";
        } else if (completedTasks > 0) {
            status = "in_progress";
        }

        return CourseProgressDto.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .totalTasks(totalTasks)
            .completedTasks(completedTasks)
            .status(status)
            .build();
    }
}