package com.wpss.wordpresssass.post.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.post.application.command.CreatePostCommand;
import com.wpss.wordpresssass.post.application.dto.PostDto;
import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.post.domain.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostApplicationService {

    private final PostRepository postRepository;

    public PostApplicationService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostDto createPost(CreatePostCommand command) {
        Long tenantId = requireTenantId();
        Post post = Post.createDraft(tenantId, command.title().trim(), command.content().trim());
        return PostDto.from(postRepository.save(post));
    }

    public List<PostDto> listPosts() {
        Long tenantId = requireTenantId();
        return postRepository.findByTenantId(tenantId)
                .stream()
                .map(PostDto::from)
                .toList();
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
