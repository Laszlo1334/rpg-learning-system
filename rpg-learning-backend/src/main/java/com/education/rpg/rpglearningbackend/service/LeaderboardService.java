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
    private final com.education.rpg.rpglearningbackend.repository.InventoryRepository inventoryRepository;

    public List<CourseLeaderboardDto> getCourseLeaderboard(Long courseId) {
        List<CourseLeaderboardDto> leaderboard = userRepository.getLeaderboardByCourseId(courseId);
        
        for (CourseLeaderboardDto dto : leaderboard) {
            userRepository.findById(dto.getUserId()).ifPresent(user -> {
                List<com.education.rpg.rpglearningbackend.model.Inventory> equippedItems = inventoryRepository.findByUserIdAndIsEquippedTrue(user.getId());

                String equippedAvatarUrl = equippedItems.stream()
                        .filter(inv -> inv.getItem().getSlot() == com.education.rpg.rpglearningbackend.model.Item.ItemSlot.AVATAR)
                        .map(inv -> inv.getItem().getAssetUrl())
                        .findFirst()
                        .orElse(user.getAvatarUrl());
                dto.setAvatarUrl(equippedAvatarUrl);
            });
        }
        
        return leaderboard;
    }

    public List<com.education.rpg.rpglearningbackend.dto.LeaderboardDto> getGlobalLeaderboard(String sortBy) {
        List<com.education.rpg.rpglearningbackend.model.User> topUsers;
        if ("gold".equalsIgnoreCase(sortBy)) {
            topUsers = userRepository.findTop10ByIsPublicProfileTrueOrderByLifetimeGoldDesc();
        } else {
            topUsers = userRepository.findTop10ByIsPublicProfileTrueOrderByCurrentXpDesc();
        }

        return topUsers.stream()
                .map(user -> {
                    com.education.rpg.rpglearningbackend.dto.LeaderboardDto dto = new com.education.rpg.rpglearningbackend.dto.LeaderboardDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setLevel(user.getLevel());
                    dto.setXp(Long.valueOf(user.getCurrentXp()));
                    dto.setLifetimeGold(user.getLifetimeGold() != null ? user.getLifetimeGold() : 0);
                    
                    List<com.education.rpg.rpglearningbackend.model.Inventory> equippedItems = inventoryRepository.findByUserIdAndIsEquippedTrue(user.getId());
                    String equippedAvatarUrl = equippedItems.stream()
                            .filter(inv -> inv.getItem().getSlot() == com.education.rpg.rpglearningbackend.model.Item.ItemSlot.AVATAR)
                            .map(inv -> inv.getItem().getAssetUrl())
                            .findFirst()
                            .orElse(user.getAvatarUrl());
                    dto.setAvatarUrl(equippedAvatarUrl);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}