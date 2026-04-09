package com.wpss.wordpresssass.common;

import com.wpss.wordpresssass.AuthTestSupport;
import com.wpss.wordpresssass.auth.infrastructure.mapper.UserAccountMapper;
import com.wpss.wordpresssass.publish.infrastructure.dataobject.PostPublishDO;
import com.wpss.wordpresssass.publish.infrastructure.mapper.PostPublishMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class TenantIsolationMapperTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private PostPublishMapper postPublishMapper;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldRejectCrossTenantUserUpdateAtSqlLayer() throws Exception {
        LoginSession owner = ensureAndLogin("tenant_guard_owner", "admin123", "Tenant Guard Owner");
        LoginSession stranger = ensureAndLogin("tenant_guard_stranger", "admin123", "Tenant Guard Stranger");

        jdbcTemplate.update(
                "INSERT INTO user(tenant_id, username, password, email, nickname, role, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                owner.tenantId(),
                "tenant_guard_member",
                passwordEncoder.encode("admin123"),
                "tenant_guard_member@example.com",
                "tenant_guard_member",
                "ADMIN",
                "ACTIVE",
                Timestamp.valueOf(LocalDateTime.now())
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM user WHERE tenant_id = ? AND username = ?",
                Long.class,
                owner.tenantId(),
                "tenant_guard_member"
        );

        int updated = userAccountMapper.updateStatus(userId, stranger.tenantId(), "DISABLED");

        assertEquals(0, updated);
        assertEquals(
                "ACTIVE",
                jdbcTemplate.queryForObject("SELECT status FROM user WHERE id = ?", String.class, userId)
        );
    }

    @Test
    void shouldRejectCrossTenantPublishRecordUpdateAtSqlLayer() throws Exception {
        LoginSession owner = ensureAndLogin("tenant_publish_owner", "admin123", "Tenant Publish Owner");
        LoginSession stranger = ensureAndLogin("tenant_publish_stranger", "admin123", "Tenant Publish Stranger");

        jdbcTemplate.update(
                """
                INSERT INTO post_publish(
                    tenant_id, post_id, site_id, idempotency_key, publish_status, target_status,
                    retry_count, max_retry_count, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                owner.tenantId(),
                1001L,
                2001L,
                owner.tenantId() + ":1001:2001:publish",
                "PENDING",
                "publish",
                0,
                3,
                Timestamp.valueOf(LocalDateTime.now())
        );

        Long publishId = jdbcTemplate.queryForObject(
                "SELECT id FROM post_publish WHERE tenant_id = ? AND post_id = ?",
                Long.class,
                owner.tenantId(),
                1001L
        );

        PostPublishDO postPublishDO = new PostPublishDO();
        postPublishDO.setId(publishId);
        postPublishDO.setTenantId(stranger.tenantId());
        postPublishDO.setPublishStatus("FAILED");
        postPublishDO.setLastHttpStatus(500);
        postPublishDO.setErrorMessage("forbidden cross tenant update");
        postPublishDO.setRetryCount(1);
        postPublishDO.setMaxRetryCount(3);
        postPublishDO.setFinishedAt(LocalDateTime.now());

        int updated = postPublishMapper.update(postPublishDO);

        assertEquals(0, updated);
        assertEquals(
                "PENDING",
                jdbcTemplate.queryForObject("SELECT publish_status FROM post_publish WHERE id = ?", String.class, publishId)
        );
    }
}
