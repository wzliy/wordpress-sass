package com.wpss.wordpresssass.publish.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.post.domain.PostRepository;
import com.wpss.wordpresssass.publish.application.command.PublishPostCommand;
import com.wpss.wordpresssass.publish.application.dto.PublishPostResultDto;
import com.wpss.wordpresssass.publish.application.dto.PublishRecordDto;
import com.wpss.wordpresssass.publish.application.dto.PublishSiteResultDto;
import com.wpss.wordpresssass.publish.domain.PostPublish;
import com.wpss.wordpresssass.publish.domain.PostPublishRepository;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublishApplicationService {

    private static final String TARGET_STATUS = "publish";
    private static final int MAX_RETRY_COUNT = 3;

    private final PostRepository postRepository;
    private final SiteRepository siteRepository;
    private final PostPublishRepository postPublishRepository;
    private final PublishTaskExecutor publishTaskExecutor;

    public PublishApplicationService(PostRepository postRepository,
                                     SiteRepository siteRepository,
                                     PostPublishRepository postPublishRepository,
                                     PublishTaskExecutor publishTaskExecutor) {
        this.postRepository = postRepository;
        this.siteRepository = siteRepository;
        this.postPublishRepository = postPublishRepository;
        this.publishTaskExecutor = publishTaskExecutor;
    }

    public PublishPostResultDto publish(PublishPostCommand command) {
        Long tenantId = requireTenantId();
        Post post = postRepository.findByIdAndTenantId(command.postId(), tenantId)
                .orElseThrow(() -> new BusinessException("Post not found"));

        List<PublishSiteResultDto> results = command.siteIds().stream()
                .distinct()
                .map(siteId -> publishToSite(tenantId, post, siteId))
                .toList();

        return new PublishPostResultDto(post.getId(), results.size(), results);
    }

    public List<PublishRecordDto> listRecords() {
        Long tenantId = requireTenantId();
        Map<Long, String> postTitles = new HashMap<>();
        Map<Long, String> siteNames = new HashMap<>();

        return postPublishRepository.findByTenantId(tenantId)
                .stream()
                .map(record -> new PublishRecordDto(
                        record.getId(),
                        record.getPostId(),
                        postTitles.computeIfAbsent(record.getPostId(), id -> postRepository.findByIdAndTenantId(id, tenantId)
                                .map(Post::getTitle)
                                .orElse("Unknown Post")),
                        record.getSiteId(),
                        siteNames.computeIfAbsent(record.getSiteId(), id -> siteRepository.findByIdAndTenantId(id, tenantId)
                                .map(Site::getName)
                                .orElse("Unknown Site")),
                        record.getPublishStatus().name(),
                        record.getTargetStatus(),
                        record.getRetryCount(),
                        record.getLastHttpStatus(),
                        resolveRecordMessage(record),
                        record.getRemotePostId(),
                        record.getRemotePostUrl(),
                        record.getCreatedAt()
                ))
                .toList();
    }

    private PublishSiteResultDto publishToSite(Long tenantId, Post post, Long siteId) {
        Site site = siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
        PostPublish queuedRecord = postPublishRepository.save(
                PostPublish.pending(tenantId, post.getId(), site.getId(), TARGET_STATUS, MAX_RETRY_COUNT)
        );
        publishTaskExecutor.execute(queuedRecord, post, site);
        return PublishSiteResultDto.from(queuedRecord);
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }

    private String resolveRecordMessage(PostPublish record) {
        return switch (record.getPublishStatus()) {
            case PENDING -> "Queued for execution";
            case PROCESSING -> "Publishing in progress";
            case SUCCESS -> "Publish successful";
            case RETRY_WAIT, FAILED -> record.getErrorMessage() == null ? "Publish failed" : record.getErrorMessage();
            case CANCELED -> "Publish canceled";
        };
    }
}
