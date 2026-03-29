package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.CourseLeaderboardDto;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;

    public List<CourseLeaderboardDto> getCourseLeaderboard(Long courseId) {
        return userRepository.getLeaderboardByCourseId(courseId);
    }
}