package com.wpss.wordpresssass.post.domain;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    List<Post> findByTenantId(Long tenantId);

    Optional<Post> findByIdAndTenantId(Long id, Long tenantId);
}
