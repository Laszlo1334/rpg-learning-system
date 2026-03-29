package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Рюкзак (Inventory)", description = "Керування купленими предметами гравця")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Отримати мій рюкзак", description = "Повертає список усіх предметів, які має гравець")
    public ResponseEntity<?> getMyInventory(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");

        String email = principal.getAttribute("email");
        return ResponseEntity.ok(inventoryService.getUserInventory(email));
    }

    @PostMapping("/{id}/use")
    @Operation(summary = "Використати розхідник", description = "Використовує 1 одиницю Зілля, Сувою або Руни")
    public ResponseEntity<?> useConsumable(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");

        try {
            String email = principal.getAttribute("email");
            inventoryService.useConsumable(email, id);
            return ResponseEntity.ok("Предмет успішно використано!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/equip")
    @Operation(summary = "Надягнути/Зняти косметику", description = "Перемикає статус екіпірування косметичного предмета")
    public ResponseEntity<?> toggleEquip(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Увійдіть у систему!");

        try {
            String email = principal.getAttribute("email");
            Inventory updatedEntry = inventoryService.toggleEquipCosmetic(email, id);
            return ResponseEntity.ok(updatedEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}