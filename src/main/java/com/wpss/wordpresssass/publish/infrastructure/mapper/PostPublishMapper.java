package com.wpss.wordpresssass.publish.infrastructure.mapper;

import com.wpss.wordpresssass.publish.infrastructure.dataobject.PostPublishDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostPublishMapper {

    @Insert("""
            INSERT INTO post_publish (
                tenant_id, post_id, site_id, idempotency_key, publish_status, target_status, last_http_status,
                remote_post_id, remote_post_url, error_message, response_body, retry_count, max_retry_count,
                next_retry_at, started_at, finished_at, created_at
            ) VALUES (
                #{tenantId}, #{postId}, #{siteId}, #{idempotencyKey}, #{publishStatus}, #{targetStatus}, #{lastHttpStatus},
                #{remotePostId}, #{remotePostUrl}, #{errorMessage}, #{responseBody}, #{retryCount}, #{maxRetryCount},
                #{nextRetryAt}, #{startedAt}, #{finishedAt}, #{createdAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostPublishDO postPublishDO);

    @Select("""
            SELECT id, tenant_id, post_id, site_id, idempotency_key, publish_status, target_status,
                   last_http_status, remote_post_id, remote_post_url, error_message, response_body,
                   retry_count, max_retry_count, next_retry_at, started_at, finished_at, created_at
            FROM post_publish
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            """)
    List<PostPublishDO> selectByTenantId(Long tenantId);

    @Update("""
            UPDATE post_publish
            SET publish_status = #{publishStatus},
                last_http_status = #{lastHttpStatus},
                remote_post_id = #{remotePostId},
                remote_post_url = #{remotePostUrl},
                error_message = #{errorMessage},
                response_body = #{responseBody},
                retry_count = #{retryCount},
                max_retry_count = #{maxRetryCount},
                next_retry_at = #{nextRetryAt},
                started_at = #{startedAt},
                finished_at = #{finishedAt}
            WHERE id = #{id}
              AND tenant_id = #{tenantId}
            """)
    int update(PostPublishDO postPublishDO);
}
