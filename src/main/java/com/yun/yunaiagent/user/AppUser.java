package com.yun.yunaiagent.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(length = 160)
    private String email;

    @Column(length = 80)
    private String role;

    @Column(length = 1000)
    private String bio;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static AppUser create(String username, String passwordHash, String displayName, String email) {
        LocalDateTime now = LocalDateTime.now();
        AppUser user = new AppUser();
        user.username = username;
        user.passwordHash = passwordHash;
        user.displayName = displayName;
        user.email = email;
        user.role = "AI 应用体验官";
        user.bio = "";
        user.createdAt = now;
        user.updatedAt = now;
        return user;
    }

    public void updateProfile(String displayName, String email, String role, String bio) {
        this.displayName = normalize(displayName, this.displayName);
        this.email = normalize(email, "");
        this.role = normalize(role, "");
        this.bio = normalize(bio, "");
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAvatarUrl(String avatarUrl) {
        this.avatarUrl = normalize(avatarUrl, "");
        this.updatedAt = LocalDateTime.now();
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
