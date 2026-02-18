package com.education.rpg.rpglearningbackend.dto;

public class LoginRequest {
    private String username;
    private String password;

    // Геттери та Сеттери
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}