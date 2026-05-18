package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.model.*;
import com.education.rpg.rpglearningbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ActivityLogService activityLogService;

    // Отримати список усіх товарів у магазині
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Inventory buyItem(String email, Long itemId) {
        // 1. Знаходимо гравця
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        // 2. Знаходимо предмет
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Предмет не знайдено в магазині"));

        // 3. ПЕРЕВІРКА ВАЛЮТИ ТА СПИСАННЯ
        if (item.getCurrencyType() == Item.CurrencyType.GOLD) {
            if (player.getGold() < item.getPrice()) {
                throw new RuntimeException(
                        "Недостатньо Золота! Вам потрібно ще " + (item.getPrice() - player.getGold()) + " 🪙");
            }
            player.setGold(player.getGold() - item.getPrice());

        } else if (item.getCurrencyType() == Item.CurrencyType.CRYSTAL) {
            if (player.getCrystals() < item.getPrice()) {
                throw new RuntimeException("Недостатньо Кристалів Невдачі! Робіть більше спроб. Вам потрібно ще "
                        + (item.getPrice() - player.getCrystals()) + " 💎");
            }
            player.setCrystals(player.getCrystals() - item.getPrice());
        }

        // 4. ЛОГІКА ІНВЕНТАРЮ (Косметика vs Розхідники)
        Inventory inventoryEntry;

        if (item.getCategory() == Item.ItemCategory.COSMETIC) {
            // Косметику купуємо лише один раз
            boolean alreadyOwns = inventoryRepository.existsByUserAndItem(player, item);
            if (alreadyOwns) {
                throw new RuntimeException("У вас вже є цей предмет гардеробу!");
            }
            inventoryEntry = new Inventory();
            inventoryEntry.setUser(player);
            inventoryEntry.setItem(item);
            inventoryEntry.setIsEquipped(false);
            inventoryEntry.setQuantity(1); // Навіть для косметики ставимо 1

        } else {
            // Розхідники (Consumables) - їх можна купувати багато разів (стакаються)
            Optional<Inventory> existingItemOpt = inventoryRepository.findByUserAndItem(player, item);
            if (existingItemOpt.isPresent()) {
                inventoryEntry = existingItemOpt.get();
                inventoryEntry.setQuantity(inventoryEntry.getQuantity() + 1); // Збільшуємо кількість на 1
            } else {
                inventoryEntry = new Inventory();
                inventoryEntry.setUser(player);
                inventoryEntry.setItem(item);
                inventoryEntry.setIsEquipped(false);
                inventoryEntry.setQuantity(1); // Перший такий предмет у рюкзаку
            }
        }

        userRepository.save(player);

        // 5. ЗБЕРЕЖЕННЯ ТРАНЗАКЦІЇ
        TransactionHistory transaction = TransactionHistory.builder()
                .user(player)
                .item(item)
                .cost(item.getPrice())
                .currencyUsed(item.getCurrencyType())
                .build();
        transactionHistoryRepository.save(transaction);

        log.info("Гравець {} успішно купив предмет: {} за {} {}",
                player.getEmail(), item.getName(), item.getPrice(), item.getCurrencyType());

        activityLogService.log(player, ActionType.ITEM_BOUGHT,
                String.format("{\"itemId\":%d,\"itemName\":\"%s\",\"price\":%d,\"currency\":\"%s\"}",
                        item.getId(), item.getName().replace("\"", "\\\""), item.getPrice(), item.getCurrencyType()));

        return inventoryRepository.save(inventoryEntry);
    }
}
