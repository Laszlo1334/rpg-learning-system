package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.LeaderboardDto;
import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Метод для отримання таблиці лідерів
    public List<LeaderboardDto> getLeaderboard() {
        // Звертаємося до нашого нового методу в репозиторії
        List<User> topStudents = userRepository.findTop10ByRoleOrderByXpDesc(Role.STUDENT);

        // Перетворюємо повних юзерів (з паролями та email) на безпечні DTO для фронтенду
        return topStudents.stream().map(student -> {
            LeaderboardDto dto = new LeaderboardDto();
            dto.setId(student.getId());
            dto.setUsername(student.getUsername());
            dto.setLevel(student.getLevel());
            dto.setXp(student.getXp());
            dto.setAvatarUrl(student.getAvatarUrl());
            return dto;
        }).collect(Collectors.toList());
    }

}