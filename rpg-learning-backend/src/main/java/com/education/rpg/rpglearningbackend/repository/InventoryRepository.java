package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.model.Item;
import com.education.rpg.rpglearningbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // Отримати всі предмети конкретного юзера
    List<Inventory> findByUserId(Long userId);

    // Знайти тільки ті предмети, які зараз надягнуті
    List<Inventory> findByUserIdAndIsEquippedTrue(Long userId);

    boolean existsByUserAndItem(User user, Item item);
}