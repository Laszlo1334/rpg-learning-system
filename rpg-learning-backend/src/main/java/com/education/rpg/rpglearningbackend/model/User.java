package com.education.rpg.rpglearningbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Long xp = 0L;

    @Column(nullable = false)
    private Long coins = 0L;

    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 1. Порожній конструктор (обов'язково для Hibernate)
    public User() {}

    // 2. Метод перед збереженням
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.level == null) this.level = 1;
        if (this.xp == null) this.xp = 0L;
        if (this.coins == null) this.coins = 0L;
    }

    // 3. ГЕТТЕРИ ТА СЕТТЕРИ (Те, що не бачила твоя IDE)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Long getXp() { return xp; }
    public void setXp(Long xp) { this.xp = xp; }

    public Long getCoins() { return coins; }
    public void setCoins(Long coins) { this.coins = coins; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}