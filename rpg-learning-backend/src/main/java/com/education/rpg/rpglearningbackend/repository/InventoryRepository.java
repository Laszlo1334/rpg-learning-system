package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.model.Item;
import com.education.rpg.rpglearningbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByUserId(Long userId);

    List<Inventory> findAllByUser(User user);

    List<Inventory> findByUserIdAndIsEquippedTrue(Long userId);

    // Ownership-safe lookup — used to prevent equip/unequip of another user's item
    Optional<Inventory> findByIdAndUserId(Long id, Long userId);

    // Used to block duplicate cosmetic purchases
    boolean existsByUserAndItem(User user, Item item);

    // Used to increment quantity when a consumable is purchased again
    Optional<Inventory> findByUserAndItem(User user, Item item);
}