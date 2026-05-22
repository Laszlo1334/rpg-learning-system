package com.education.rpg.rpglearningbackend.service;

import com.education.rpg.rpglearningbackend.dto.RegisterRequest;
import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.model.Item;
import com.education.rpg.rpglearningbackend.model.Role;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.InventoryRepository;
import com.education.rpg.rpglearningbackend.repository.ItemRepository;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       ItemRepository itemRepository, InventoryRepository inventoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.itemRepository = itemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Role role = Role.STUDENT;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("TEACHER")) {
            role = Role.TEACHER;
        } else if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = Role.ADMIN;
        }


        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        // Assign a default avatar so the leaderboard never shows a broken image
        user.setAvatarUrl("/assets/default_avatar.png");
        user.setTotalLoginDays(0);
        user.setLongestLoginStreak(0);
        user.setTotalPlayTimeSeconds(0L);
        user.setCurrentFlawlessStreak(0);
        user.setLongestFlawlessStreak(0);

        User savedUser = userRepository.save(user);

        // Grant the default avatar item to every new user on registration
        Optional<Item> defaultAvatarOpt = itemRepository.findByName("Базовий Аватар");
        if (defaultAvatarOpt.isPresent()) {
            Inventory inventory = new Inventory();
            inventory.setUser(savedUser);
            inventory.setItem(defaultAvatarOpt.get());
            inventory.setIsEquipped(true);
            inventory.setQuantity(1);
            inventoryRepository.save(inventory);
        }

        return savedUser;
    }
}