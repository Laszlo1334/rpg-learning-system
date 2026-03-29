package com.education.rpg.rpglearningbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID залишаємо відкритим, він нам потрібен для фронтенду!

    @JsonIgnore // ХОВАЄМО ЮЗЕРА ТУТ, щоб не було помилки 500 (LazyInitializationException)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ЗМІНЮЄМО НА EAGER, щоб фронтенд одразу бачив картинку і назву купленого предмета
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Boolean isEquipped = false; // Чи надягнуто зараз? (Для косметики)

    // Кількість предметів у стаці (для розхідників)
    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        this.purchasedAt = LocalDateTime.now();
    }
}