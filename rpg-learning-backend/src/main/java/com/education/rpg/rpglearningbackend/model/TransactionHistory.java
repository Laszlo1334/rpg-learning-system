package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Data
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Integer cost; // Скільки було витрачено

    @Enumerated(EnumType.STRING)
    private Item.CurrencyType currencyUsed; // Золото чи Кристали

    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        this.purchasedAt = LocalDateTime.now();
    }
}