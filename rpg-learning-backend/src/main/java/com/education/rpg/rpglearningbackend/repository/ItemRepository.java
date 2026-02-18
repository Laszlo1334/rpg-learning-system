package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // Можна буде додати пошук за типом предмета (наприклад, тільки капелюхи)
}