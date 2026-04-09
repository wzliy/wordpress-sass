package com.wpss.wordpresssass.post.application.dto;

import com.wpss.wordpresssass.post.domain.Post;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        Long tenantId,
        String title,
        String content,
        String status,
        LocalDateTime createdAt
) {

    public static PostDto from(Post post) {
        return new PostDto(
                post.getId(),
                post.getTenantId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus().name(),
                post.getCreatedAt()
        );
    }
}
