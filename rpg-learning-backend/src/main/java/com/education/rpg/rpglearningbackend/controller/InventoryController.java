package com.education.rpg.rpglearningbackend.controller;

import com.education.rpg.rpglearningbackend.model.Inventory;
import com.education.rpg.rpglearningbackend.model.User;
import com.education.rpg.rpglearningbackend.repository.UserRepository;
import com.education.rpg.rpglearningbackend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Player inventory management")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get my inventory", description = "Returns all items the player owns")
    public ResponseEntity<?> getMyInventory(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");

        String email = principal.getAttribute("email");
        return ResponseEntity.ok(inventoryService.getUserInventory(email));
    }

    @PostMapping("/{id}/use")
    @Operation(summary = "Use a consumable", description = "Uses 1 unit of a potion/scroll/rune and applies its effect")
    public ResponseEntity<?> useConsumable(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");

        try {
            String email = principal.getAttribute("email");
            inventoryService.useConsumable(email, id);
            return ResponseEntity.ok("Item used successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/equip")
    @Operation(summary = "Equip a cosmetic item", description = "Equips the item and optionally replaces a specific currently-equipped item in the same slot")
    public ResponseEntity<?> equipItem(
            @PathVariable Long id,
            @RequestParam(required = false) Long replaceId,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");

        try {
            String email = principal.getAttribute("email");
            User player = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            inventoryService.equipItem(id, player.getId(), replaceId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}