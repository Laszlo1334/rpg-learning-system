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

    // Fetch all inventory entries by user id
    List<Inventory> findByUserId(Long userId);

    // Fetch all inventory entries by User object
    List<Inventory> findAllByUser(User user);

    // Fetch all currently equipped items for a user
    List<Inventory> findByUserIdAndIsEquippedTrue(Long userId);

    // Find a specific inventory entry belonging to a user (used for equip/unequip security check)
    Optional<Inventory> findByIdAndUserId(Long id, Long userId);

    // Check ownership for cosmetics (prevents duplicate purchases)
    boolean existsByUserAndItem(User user, Item item);

    // Find a specific item entry for stacking (consumable quantity management)
    Optional<Inventory> findByUserAndItem(User user, Item item);
}