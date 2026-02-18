package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ItemType type;

    private Integer price; // Ціна в монетах

    private Integer minLevelReq; // З якого рівня можна купити

    private String assetUrl; // Посилання на картинку (PNG/SVG)
}