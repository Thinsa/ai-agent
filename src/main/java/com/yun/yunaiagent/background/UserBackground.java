package com.yun.yunaiagent.background;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_background", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_background_user_agent", columnNames = {"user_id", "agent_key"})
})
public class UserBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "agent_key", nullable = false, length = 32)
    private String agentKey;

    @Lob
    @Column(name = "image_data", columnDefinition = "text")
    private String imageData;

    @Column(nullable = false)
    private double opacity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserBackground create(Long userId, String agentKey, String imageData, double opacity) {
        LocalDateTime now = LocalDateTime.now();
        UserBackground bg = new UserBackground();
        bg.userId = userId;
        bg.agentKey = agentKey;
        bg.imageData = imageData;
        bg.opacity = opacity;
        bg.createdAt = now;
        bg.updatedAt = now;
        return bg;
    }

    public void update(String imageData, double opacity) {
        this.imageData = imageData;
        this.opacity = opacity;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAgentKey() { return agentKey; }
    public String getImageData() { return imageData; }
    public double getOpacity() { return opacity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
