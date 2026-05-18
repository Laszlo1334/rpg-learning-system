package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.ActionType;
import com.education.rpg.rpglearningbackend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Optional<ActivityLog> findTopByUserIdAndActionTypeOrderByTimestampDesc(Long userId, ActionType actionType);
}
