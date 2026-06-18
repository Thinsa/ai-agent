package com.yun.yunaiagent.storysave;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_save")
public class StorySave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "story_id", nullable = false, length = 64)
    private String storyId;

    @Column(name = "scene_id", nullable = false, length = 64)
    private String sceneId;

    @Column(name = "choice_history", columnDefinition = "text")
    private String choiceHistory;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    public static StorySave create(Long userId, String storyId, String sceneId, String choiceHistory) {
        StorySave save = new StorySave();
        save.userId = userId;
        save.storyId = storyId;
        save.sceneId = sceneId;
        save.choiceHistory = choiceHistory;
        save.savedAt = LocalDateTime.now();
        return save;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getStoryId() { return storyId; }
    public String getSceneId() { return sceneId; }
    public String getChoiceHistory() { return choiceHistory; }
    public LocalDateTime getSavedAt() { return savedAt; }
}
