package com.wpss.wordpresssass.post.infrastructure;

import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.post.domain.PostRepository;
import com.wpss.wordpresssass.post.domain.PostStatus;
import com.wpss.wordpresssass.post.infrastructure.dataobject.PostDO;
import com.wpss.wordpresssass.post.infrastructure.mapper.PostMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisPostRepository implements PostRepository {

    private final PostMapper postMapper;

    public MybatisPostRepository(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    public Post save(Post post) {
        PostDO postDO = toDataObject(post);
        postMapper.insert(postDO);
        return toDomain(postDO);
    }

    @Override
    public List<Post> findByTenantId(Long tenantId) {
        return postMapper.selectByTenantId(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Post> findByIdAndTenantId(Long id, Long tenantId) {
        return postMapper.selectByIdAndTenantId(id, tenantId)
                .map(this::toDomain);
    }

    private PostDO toDataObject(Post post) {
        PostDO postDO = new PostDO();
        postDO.setId(post.getId());
        postDO.setTenantId(post.getTenantId());
        postDO.setTitle(post.getTitle());
        postDO.setContent(post.getContent());
        postDO.setStatus(post.getStatus().name());
        postDO.setCreatedAt(post.getCreatedAt());
        return postDO;
    }

    private Post toDomain(PostDO postDO) {
        return new Post(
                postDO.getId(),
                postDO.getTenantId(),
                postDO.getTitle(),
                postDO.getContent(),
                PostStatus.valueOf(postDO.getStatus()),
                postDO.getCreatedAt()
        );
    }
}
