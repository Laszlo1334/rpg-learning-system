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
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));
        return inventoryRepository.findAllByUser(player);
    }

    @Transactional
    public void useConsumable(String email, Long inventoryId) {
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        Inventory inventoryEntry = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Предмет не знайдено в інвентарі"));

        if (!inventoryEntry.getUser().getId().equals(player.getId())) {
            throw new RuntimeException("Це не ваш предмет!");
        }

        Item item = inventoryEntry.getItem();
        if (item.getCategory() != Item.ItemCategory.CONSUMABLE) {
            throw new RuntimeException("Цей предмет не можна використати таким чином!");
        }

        // --- МАГІЯ БАФІВ (Без Сувою) ---
        LocalDateTime now = LocalDateTime.now();
        switch (item.getEffect()) {
            case XP_BOOST_30_MIN:
                player.setXpBuffEndsAt(now.plusMinutes(30));
                break;
            case GOLD_BOOST_60_MIN:
                player.setGoldBuffEndsAt(now.plusMinutes(60));
                break;
            case ENERGY_STASIS_30_MIN:
                player.setEnergyStasisEndsAt(now.plusMinutes(30));
                break;
            case SINGLE_RUN_SHIELD:
                if (player.getHasActiveShield() != null && player.getHasActiveShield()) {
                    throw new RuntimeException("Щит вже активний!");
                }
                player.setHasActiveShield(true);
                break;
            case NONE:
            default:
                break;
        }

        int currentQuantity = inventoryEntry.getQuantity();
        if (currentQuantity <= 1) {
            inventoryRepository.delete(inventoryEntry);
        } else {
            inventoryEntry.setQuantity(currentQuantity - 1);
            inventoryRepository.save(inventoryEntry);
        }

        userRepository.save(player);
        log.info("Гравець {} активував ефект: {}", email, item.getEffect());
    }

    @Transactional
    public Inventory toggleEquipCosmetic(String email, Long inventoryId) {
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        Inventory inventoryEntry = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Предмет не знайдено в інвентарі"));

        if (!inventoryEntry.getUser().getId().equals(player.getId())) {
            throw new RuntimeException("Це не ваш предмет!");
        }

        if (inventoryEntry.getItem().getCategory() != Item.ItemCategory.COSMETIC) {
            throw new RuntimeException("Розхідники не можна надягати!");
        }

        boolean isCurrentlyEquipped = inventoryEntry.getIsEquipped();
        inventoryEntry.setIsEquipped(!isCurrentlyEquipped);

        return inventoryRepository.save(inventoryEntry);
    }
}