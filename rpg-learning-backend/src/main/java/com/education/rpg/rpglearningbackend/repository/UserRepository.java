package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.dto.CourseLeaderboardDto;
import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Метод для входу через Google (залишив один, без дублікатів)
    Optional<User> findByEmail(String email);

    // 1. Для таблиці лідерів (Топ гравців) - ЗМІНЕНО НА currentXp
    List<User> findAllByOrderByCurrentXpDesc();

    // 2. Для адмінки (Знайти всіх вчителів або всіх студентів)
    List<User> findByRole(Role role);

    // ЗМІНЕНО: Тепер беремо лише тих студентів, у яких isPublicProfile == true,
    // і сортуємо за currentXp.
    List<User> findTop10ByRoleAndIsPublicProfileTrueOrderByCurrentXpDesc(Role role);

    // Для Глобального Лідерборду
    List<User> findTop10ByIsPublicProfileTrueOrderByCurrentXpDesc();
    List<User> findTop10ByIsPublicProfileTrueOrderByLifetimeGoldDesc();

    // --- НОВЕ: МІКРО-ЛІДЕРБОРД ДЛЯ КОНКРЕТНОГО КУРСУ ---
    @Query("SELECT new com.education.rpg.rpglearningbackend.dto.CourseLeaderboardDto(u.id, u.username, SUM(t.rewardXp)) " +
            "FROM CompletedTask ct " +
            "JOIN ct.user u " +
            "JOIN ct.task t " +
            "WHERE t.course.id = :courseId AND u.isPublicProfile = true " +
            "GROUP BY u.id, u.username " +
            "ORDER BY SUM(t.rewardXp) DESC")
    List<CourseLeaderboardDto> getLeaderboardByCourseId(@Param("courseId") Long courseId);

    // --- SCHEDULER: Масове скидання енергії всіх гравців ---
    @Modifying
    @Query("UPDATE User u SET u.energy = 100")
    void resetAllUsersEnergy();
}