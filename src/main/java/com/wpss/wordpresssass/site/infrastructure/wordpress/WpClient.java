package com.wpss.wordpresssass.site.infrastructure.wordpress;

import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.site.domain.Site;

public interface WpClient {

    WpConnectionResult testConnection(Site site);

    WpPublishResult publishPost(Site site, Post post, String targetStatus);
}
