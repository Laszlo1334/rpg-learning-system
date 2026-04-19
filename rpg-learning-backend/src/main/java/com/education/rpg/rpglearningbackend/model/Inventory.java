package com.education.rpg.rpglearningbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID remains public — frontend needs it for use/equip calls

    @JsonIgnore // Prevent circular reference / LazyInitializationException
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // EAGER so the frontend immediately gets item name, price, assetUrl
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @JsonProperty("isEquipped") // Fix Lombok is-prefix → Jackson strips "is" → "equipped"
    @Column(nullable = false)
    private Boolean isEquipped = false;

    // Stack count for consumables (cosmetics always = 1)
    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        this.purchasedAt = LocalDateTime.now();
    }
}