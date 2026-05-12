package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Знайти предмети за категорією (наприклад, тільки CONSUMABLE)
    List<Item> findByCategory(Item.ItemCategory category);

    // Перевірка існування предмета за назвою (захист від дублікатів у сідері)
    boolean existsByName(String name);

    // Знайти предмет за назвою
    java.util.Optional<Item> findByName(String name);
}