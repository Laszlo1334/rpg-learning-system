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

    // Отримати всі предмети за ID юзера
    List<Inventory> findByUserId(Long userId);

    // НОВИЙ МЕТОД: Отримати всі предмети, передавши об'єкт User (Вирішує нашу помилку!)
    List<Inventory> findAllByUser(User user);

    // Знайти тільки ті предмети, які зараз надягнуті
    List<Inventory> findByUserIdAndIsEquippedTrue(Long userId);

    // Перевірка наявності для Косметики
    boolean existsByUserAndItem(User user, Item item);

    // Знайти конкретний предмет гравця для стакування (збільшення/зменшення quantity)
    Optional<Inventory> findByUserAndItem(User user, Item item);
}