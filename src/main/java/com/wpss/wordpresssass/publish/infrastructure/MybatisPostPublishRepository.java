package com.wpss.wordpresssass.publish.infrastructure;

import com.wpss.wordpresssass.publish.domain.PostPublish;
import com.wpss.wordpresssass.publish.domain.PostPublishRepository;
import com.wpss.wordpresssass.publish.domain.PublishStatus;
import com.wpss.wordpresssass.publish.infrastructure.dataobject.PostPublishDO;
import com.wpss.wordpresssass.publish.infrastructure.mapper.PostPublishMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisPostPublishRepository implements PostPublishRepository {

    private final PostPublishMapper postPublishMapper;

    public MybatisPostPublishRepository(PostPublishMapper postPublishMapper) {
        this.postPublishMapper = postPublishMapper;
    }

    @Override
    public PostPublish save(PostPublish postPublish) {
        PostPublishDO postPublishDO = toDataObject(postPublish);
        postPublishMapper.insert(postPublishDO);
        return toDomain(postPublishDO);
    }

    @Override
    public List<PostPublish> findByTenantId(Long tenantId) {
        return postPublishMapper.selectByTenantId(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void update(PostPublish postPublish) {
        int updated = postPublishMapper.update(toDataObject(postPublish));
        if (updated != 1) {
            throw new IllegalStateException("Post publish record update violated tenant isolation");
        }
    }

    private PostPublishDO toDataObject(PostPublish postPublish) {
        PostPublishDO postPublishDO = new PostPublishDO();
        postPublishDO.setId(postPublish.getId());
        postPublishDO.setTenantId(postPublish.getTenantId());
        postPublishDO.setPostId(postPublish.getPostId());
        postPublishDO.setSiteId(postPublish.getSiteId());
        postPublishDO.setIdempotencyKey(postPublish.getIdempotencyKey());
        postPublishDO.setPublishStatus(postPublish.getPublishStatus().name());
        postPublishDO.setTargetStatus(postPublish.getTargetStatus());
        postPublishDO.setLastHttpStatus(postPublish.getLastHttpStatus());
        postPublishDO.setRemotePostId(postPublish.getRemotePostId());
        postPublishDO.setRemotePostUrl(postPublish.getRemotePostUrl());
        postPublishDO.setErrorMessage(postPublish.getErrorMessage());
        postPublishDO.setResponseBody(postPublish.getResponseBody());
        postPublishDO.setRetryCount(postPublish.getRetryCount());
        postPublishDO.setMaxRetryCount(postPublish.getMaxRetryCount());
        postPublishDO.setNextRetryAt(postPublish.getNextRetryAt());
        postPublishDO.setStartedAt(postPublish.getStartedAt());
        postPublishDO.setFinishedAt(postPublish.getFinishedAt());
        postPublishDO.setCreatedAt(postPublish.getCreatedAt());
        return postPublishDO;
    }

    private PostPublish toDomain(PostPublishDO postPublishDO) {
        return new PostPublish(
                postPublishDO.getId(),
                postPublishDO.getTenantId(),
                postPublishDO.getPostId(),
                postPublishDO.getSiteId(),
                postPublishDO.getIdempotencyKey(),
                PublishStatus.valueOf(postPublishDO.getPublishStatus()),
                postPublishDO.getTargetStatus(),
                postPublishDO.getLastHttpStatus(),
                postPublishDO.getRemotePostId(),
                postPublishDO.getRemotePostUrl(),
                postPublishDO.getErrorMessage(),
                postPublishDO.getResponseBody(),
                postPublishDO.getRetryCount(),
                postPublishDO.getMaxRetryCount(),
                postPublishDO.getNextRetryAt(),
                postPublishDO.getStartedAt(),
                postPublishDO.getFinishedAt(),
                postPublishDO.getCreatedAt()
        );
    }
}
