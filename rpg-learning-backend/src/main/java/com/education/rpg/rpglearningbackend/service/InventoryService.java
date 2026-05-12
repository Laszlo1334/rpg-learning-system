package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.model.Item;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.InventoryRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public List<Inventory> getUserInventory(String email) {
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        return inventoryRepository.findAllByUser(player);
    }

    @Transactional
    public void useConsumable(String email, Long inventoryId) {
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        Inventory inventoryEntry = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Item not found in inventory"));

        if (!inventoryEntry.getUser().getId().equals(player.getId())) {
            throw new RuntimeException("This item does not belong to you!");
        }

        Item item = inventoryEntry.getItem();
        if (item.getCategory() != Item.ItemCategory.CONSUMABLE) {
            throw new RuntimeException("This item cannot be used this way!");
        }

        // ── Apply buff effect ─────────────────────────────────────────
        LocalDateTime now = LocalDateTime.now();
        switch (item.getEffect()) {
            case XP_BOOST:
                player.setXpBuffEndsAt(now.plusMinutes(30));
                log.info("Player {} activated XP_BOOST until {}", email, player.getXpBuffEndsAt());
                break;

            case GOLD_BOOST:
                player.setGoldBuffEndsAt(now.plusMinutes(60));
                log.info("Player {} activated GOLD_BOOST until {}", email, player.getGoldBuffEndsAt());
                break;

            case ENERGY_REFILL:
                player.setEnergy(100);
                log.info("Player {} restored energy to 100", email);
                break;

            case SHIELD:
                if (Boolean.TRUE.equals(player.getHasActiveShield())) {
                    throw new RuntimeException("Shield is already active!");
                }
                player.setHasActiveShield(true);
                log.info("Player {} activated SHIELD", email);
                break;

            case NONE:
            default:
                log.warn("Item '{}' has NONE effect — nothing to apply", item.getName());
                break;
        }

        // ── Consume one unit ─────────────────────────────────────────
        int currentQuantity = inventoryEntry.getQuantity();
        if (currentQuantity <= 1) {
            inventoryRepository.delete(inventoryEntry);
        } else {
            inventoryEntry.setQuantity(currentQuantity - 1);
            inventoryRepository.save(inventoryEntry);
        }

        userRepository.save(player);
    }

    @Transactional
    public Inventory toggleEquipCosmetic(String email, Long inventoryId) {
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        Inventory inventoryEntry = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Item not found in inventory"));

        if (!inventoryEntry.getUser().getId().equals(player.getId())) {
            throw new RuntimeException("This item does not belong to you!");
        }

        if (inventoryEntry.getItem().getCategory() != Item.ItemCategory.COSMETIC) {
            throw new RuntimeException("Consumables cannot be equipped!");
        }

        boolean isCurrentlyEquipped = inventoryEntry.getIsEquipped();
        inventoryEntry.setIsEquipped(!isCurrentlyEquipped);

        return inventoryRepository.save(inventoryEntry);
    }

    @Transactional
    public void equipItem(Long inventoryId, Long userId, Long replaceId) {
        Inventory entry = inventoryRepository.findByIdAndUserId(inventoryId, userId)
                .orElseThrow(() -> new RuntimeException("Item not found in inventory"));

        Item.ItemSlot slot = entry.getItem().getSlot();

        // Items with NONE slot (consumables) cannot be equipped via this endpoint
        if (slot == Item.ItemSlot.NONE) {
            throw new RuntimeException("This item has no equipment slot.");
        }

        List<Inventory> equipped = inventoryRepository.findByUserIdAndIsEquippedTrue(userId);

        // 1. If a specific item was targeted for replacement (crucial for dual-wielding
        // independent slots)
        if (replaceId != null) {
            equipped.stream()
                    .filter(i -> i.getId().equals(replaceId))
                    .findFirst()
                    .ifPresent(i -> i.setIsEquipped(false));
        }

        // 2. Clear other items in the same slot.
        // For non-weapon slots (including AVATAR) always unequip all — fixes the avatar
        // stacking bug.
        // For weapons, only unequip if no specific target was given (fallback: displace
        // oldest).
        if (slot != Item.ItemSlot.WEAPON) {
            equipped.stream()
                    .filter(i -> i.getItem().getSlot() == slot && !i.getId().equals(inventoryId))
                    .forEach(i -> i.setIsEquipped(false));
        } else if (replaceId == null) {
            // Fallback for weapons when no target specified: allow max 2, displace oldest
            List<Inventory> weapons = equipped.stream()
                    .filter(i -> i.getItem().getSlot() == Item.ItemSlot.WEAPON
                            && !i.getId().equals(inventoryId))
                    .collect(Collectors.toList());
            if (weapons.size() >= 2) {
                weapons.get(0).setIsEquipped(false);
            }
        }

        entry.setIsEquipped(true);
        inventoryRepository.saveAll(equipped);
        inventoryRepository.save(entry);
    }
}