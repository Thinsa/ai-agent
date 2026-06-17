package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_conversation",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_conversation_module_chat_id", columnNames = {"module", "chat_id"}),
        indexes = {
                @Index(name = "idx_chat_conversation_user_module", columnList = "user_id,module"),
                @Index(name = "idx_chat_conversation_chat_id", columnList = "chat_id")
        }
)
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String module;

    @Column(name = "chat_id", nullable = false, length = 120)
    private String chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ChatConversation create(String module, String chatId, AppUser user, String title) {
        LocalDateTime now = LocalDateTime.now();
        ChatConversation conversation = new ChatConversation();
        conversation.module = module;
        conversation.chatId = chatId;
        conversation.user = user;
        conversation.title = title;
        conversation.createdAt = now;
        conversation.updatedAt = now;
        return conversation;
    }

    public void bindUserIfMissing(AppUser user) {
        if (this.user == null && user != null) {
            this.user = user;
        }
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getModule() {
        return module;
    }

    public String getChatId() {
        return chatId;
    }

    public AppUser getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
