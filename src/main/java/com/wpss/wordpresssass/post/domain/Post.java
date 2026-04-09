package com.wpss.wordpresssass.post.domain;

import java.time.LocalDateTime;

public class Post {

    private final Long id;
    private final Long tenantId;
    private final String title;
    private final String content;
    private final PostStatus status;
    private final LocalDateTime createdAt;

    public Post(Long id, Long tenantId, String title, String content, PostStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Post createDraft(Long tenantId, String title, String content) {
        return new Post(null, tenantId, title, content, PostStatus.DRAFT, LocalDateTime.now());
    }

    public Post withId(Long newId) {
        return new Post(newId, tenantId, title, content, status, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public PostStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
