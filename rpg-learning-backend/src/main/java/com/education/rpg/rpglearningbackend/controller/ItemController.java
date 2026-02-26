package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.model.Item;
import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Shop & Inventory", description = "Магазин та інвентар гравця")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Переглянути всі товари в магазині")
    public ResponseEntity<List<Item>> getShopItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @PostMapping("/{id}/buy")
    @Operation(summary = "Купити предмет за монети")
    public ResponseEntity<?> buyItem(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        try {
            String email = principal.getAttribute("email");
            Inventory result = itemService.buyItem(email, id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Якщо сталася помилка (немає грошей, низький рівень), повертаємо її як текст
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}