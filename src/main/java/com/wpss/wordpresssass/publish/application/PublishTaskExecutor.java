package com.wpss.wordpresssass.publish.application;

import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.publish.domain.PostPublish;
import com.wpss.wordpresssass.publish.domain.PostPublishRepository;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpClient;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpPublishResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PublishTaskExecutor {

    private static final String TARGET_STATUS = "publish";
    private static final int MAX_RETRY_COUNT = 3;

    private final PostPublishRepository postPublishRepository;
    private final WpClient wpClient;

    public PublishTaskExecutor(PostPublishRepository postPublishRepository, WpClient wpClient) {
        this.postPublishRepository = postPublishRepository;
        this.wpClient = wpClient;
    }

    @Async("publishAsyncExecutor")
    public void execute(PostPublish queuedRecord, Post post, Site site) {
        PostPublish processingRecord = queuedRecord.markProcessing();
        postPublishRepository.update(processingRecord);

        int retryCount = 0;
        WpPublishResult wpPublishResult = wpClient.publishPost(site, post, TARGET_STATUS);
        while (!wpPublishResult.success() && wpPublishResult.retryable() && retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            wpPublishResult = wpClient.publishPost(site, post, TARGET_STATUS);
        }

        PostPublish completedRecord = wpPublishResult.success()
                ? processingRecord.markSuccess(
                        wpPublishResult.httpStatus(),
                        wpPublishResult.remotePostId(),
                        wpPublishResult.remotePostUrl(),
                        wpPublishResult.responseBody(),
                        retryCount
                )
                : processingRecord.markFailure(
                        wpPublishResult.httpStatus(),
                        wpPublishResult.message(),
                        wpPublishResult.responseBody(),
                        retryCount
                );

        postPublishRepository.update(completedRecord);
    }
}
