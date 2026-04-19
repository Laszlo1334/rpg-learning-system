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
}