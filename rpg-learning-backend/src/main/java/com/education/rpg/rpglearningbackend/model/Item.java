package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(nullable = false)
    private Integer price;

    // ── Currency ──────────────────────────────────────────────────
    public enum CurrencyType {
        GOLD, CRYSTAL
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currencyType;

    // ── Category ──────────────────────────────────────────────────
    public enum ItemCategory {
        COSMETIC, CONSUMABLE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    // ── Effect (consumable mechanics) ─────────────────────────────
    public enum EffectType {
        XP_BOOST,       // +50% XP for 30 min
        GOLD_BOOST,     // ×2 Gold for 60 min
        ENERGY_REFILL,  // instantly restore energy to 100
        SHIELD,         // protect from one defeat
        NONE            // cosmetics have no effect
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EffectType effect = EffectType.NONE;

    // ── Cosmetic slot (for equip system) ─────────────────────────
    public enum ItemSlot {
        HEAD, BODY, HANDS, LEGS, WEAPON, AVATAR,
        NONE  // consumables / items without a slot
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSlot slot = ItemSlot.NONE;

    // ── Rarity ────────────────────────────────────────────────────
    public enum ItemRarity {
        COMMON, RARE, EPIC, LEGENDARY
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemRarity rarity = ItemRarity.COMMON;

    private String assetUrl;
}