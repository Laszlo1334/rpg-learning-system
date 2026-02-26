package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Метод, який ми додали спеціально для входу через Google!
    Optional<User> findByEmail(String email);

       // 1. Для таблиці лідерів (Топ гравців)
    // Spring сам зрозуміє, що треба відсортувати всіх за досвідом (XP) від найбільшого до найменшого
    List<User> findAllByOrderByXpDesc();

    // 2. Для адмінки (Знайти всіх вчителів або всіх студентів)
    List<User> findByRole(Role role);

    List<User> findTop10ByRoleOrderByXpDesc(Role role);
}