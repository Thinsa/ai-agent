package com.yun.yunaiagent.rag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 用户自定义知识库文档。
 *
 * <p>与 classpath 静态文档不同，这部分文档由用户通过前端管理，
 * 可单独启用/禁用，支持增删改操作。</p>
 */
@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, length = 20)
    private String source = "custom";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected KnowledgeDocument() {
    }

    public static KnowledgeDocument create(String title, String content, String category) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.title = title;
        doc.content = content;
        doc.category = category;
        doc.enabled = true;
        doc.source = "custom";
        doc.createdAt = LocalDateTime.now();
        doc.updatedAt = LocalDateTime.now();
        return doc;
    }

    public void update(String title, String content, String category, boolean enabled) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
