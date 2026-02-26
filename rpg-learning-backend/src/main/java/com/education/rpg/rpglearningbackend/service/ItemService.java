package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.model.*;
import com.education.rpg.rpglearningbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    // Отримати список усіх товарів у магазині
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Inventory buyItem(String email, Long itemId) {
        // 1. Знаходимо гравця
        User player = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Гравця не знайдено"));

        // 2. Знаходимо предмет, який він хоче купити
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Предмет не знайдено в магазині"));

        // 3. Перевірка 1: Чи достатньо рівня?
        if (item.getMinLevelReq() != null && player.getLevel() < item.getMinLevelReq()) {
            throw new RuntimeException("Ваш рівень занадто низький для покупки цього предмета! Необхідний рівень: " + item.getMinLevelReq());
        }

        // 4. Перевірка 2: Чи є гроші?
        if (player.getCoins() < item.getPrice()) {
            throw new RuntimeException("Недостатньо монет! Вам потрібно ще " + (item.getPrice() - player.getCoins()));
        }

        // 5. Перевірка 3: Чи є вже такий предмет в інвентарі? (щоб не купувати дублікати)
        boolean alreadyOwns = inventoryRepository.existsByUserAndItem(player, item);
        if (alreadyOwns) {
            throw new RuntimeException("У вас вже є цей предмет в інвентарі!");
        }

        // 6. Успішна покупка: Списуємо монети
        player.setCoins(player.getCoins() - item.getPrice().longValue());
        userRepository.save(player);

        // 7. Додаємо предмет в інвентар
        Inventory inventoryEntry = new Inventory();
        inventoryEntry.setUser(player);
        inventoryEntry.setItem(item);
        inventoryEntry.setIsEquipped(false); // За замовчуванням просто лежить у рюкзаку

        log.info("Гравець {} успішно купив предмет: {}", player.getEmail(), item.getName());

        return inventoryRepository.save(inventoryEntry);
    }
}