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

    public enum CurrencyType {
        GOLD, CRYSTAL
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currencyType;

    public enum ItemCategory {
        COSMETIC, CONSUMABLE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    // --- НОВЕ: ІДЕНТИФІКАТОР ЕФЕКТУ ---
    public enum ItemEffect {
        XP_BOOST_30_MIN,       // Еліксир Мудрості (x1.5 XP на 30 хв)
        GOLD_BOOST_60_MIN,     // Магніт Гобліна (x2 Золота на 60 хв)
        ENERGY_STASIS_30_MIN,  // Кава Магістра (Енергія не витрачається 30 хв)
        SINGLE_RUN_SHIELD,     // Аура Безстрашності (Захист на 1 забіг)
        NONE                   // Для косметики
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemEffect effect = ItemEffect.NONE;

    private String assetUrl;
}