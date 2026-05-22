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

    // Currency used to purchase this item
    public enum CurrencyType {
        GOLD, CRYSTAL
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currencyType;

    // Item category: cosmetic (equip-only) or consumable (activatable)
    public enum ItemCategory {
        COSMETIC, CONSUMABLE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    // Effect applied when a consumable is used
    public enum EffectType {
        XP_BOOST, // +50% XP for 30 min
        GOLD_BOOST, // ×2 Gold for 60 min
        ENERGY_REFILL, // instantly restore energy to 100
        SHIELD, // protect from one defeat
        NONE // cosmetics have no effect
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EffectType effect = EffectType.NONE;

    // Equipment slot this item occupies (NONE for consumables)
    public enum ItemSlot {
        HEAD, BODY, HANDS, LEGS, WEAPON, AVATAR,
        NONE // consumables / items without a slot
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSlot slot = ItemSlot.NONE;

    // Rarity tier used for visual distinction and drop rates
    public enum ItemRarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemRarity rarity = ItemRarity.COMMON;

    private String assetUrl;

    // ATK bonus granted by weapons; 0 for all non-weapon items
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer attributeBonus = 0;
}