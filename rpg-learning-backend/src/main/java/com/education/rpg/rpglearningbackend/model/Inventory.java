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
    private Long id; // Exposed to frontend for use/equip API calls

    @JsonIgnore // Prevents circular serialization and lazy-load exceptions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // EAGER so item details (name, price, assetUrl) are always available in the response
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @JsonProperty("isEquipped") // Lombok strips the "is" prefix; this forces the correct JSON key
    @Column(nullable = false)
    private Boolean isEquipped = false;

    // Stack count: consumables may stack; cosmetics are always 1
    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        this.purchasedAt = LocalDateTime.now();
    }
}