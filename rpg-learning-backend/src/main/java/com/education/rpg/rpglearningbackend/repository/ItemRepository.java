package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByCategory(Item.ItemCategory category);

    // Used by the database seeder to prevent duplicate item entries
    boolean existsByName(String name);

    java.util.Optional<Item> findByName(String name);
}