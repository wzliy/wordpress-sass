package com.wpss.wordpresssass.publish.domain;

import java.util.List;

public interface PostPublishRepository {

    PostPublish save(PostPublish postPublish);

    List<PostPublish> findByTenantId(Long tenantId);

    void update(PostPublish postPublish);
}
