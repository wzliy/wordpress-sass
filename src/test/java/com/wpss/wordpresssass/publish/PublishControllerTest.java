package com.wpss.wordpresssass.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.AuthTestSupport;
import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpClient;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpConnectionResult;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpPublishResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublishControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private WpClient wpClient;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldPublishPostToMultipleSites() throws Exception {
        LoginSession session = ensureAndLogin("publish_admin", "admin123", "Publish Tenant");
        Long postId = createPost(session.token(), "Publish title", "Publish content body");
        Long siteId = createSite(session.token(), "Publish Site", "https://publish.example");

        when(wpClient.publishPost(any(Site.class), any(Post.class), eq("publish")))
                .thenReturn(WpPublishResult.success(201, 101L, "https://publish.example/?p=101", "{\"id\":101}"));

        mockMvc.perform(post("/publish")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "siteIds": [%d]
                                }
                                """.formatted(postId, siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.totalSites").value(1))
                .andExpect(jsonPath("$.data.results[0].siteId").value(siteId))
                .andExpect(jsonPath("$.data.results[0].status").value("PENDING"));

        waitForPublishStatus(session.token(), "SUCCESS", Duration.ofSeconds(3));
    }

    @Test
    void shouldRecordFailedPublishAsRetryWaitForRetryableErrors() throws Exception {
        LoginSession session = ensureAndLogin("retry_publish_admin", "admin123", "Retry Publish Tenant");
        Long postId = createPost(session.token(), "Retry title", "Retry content body");
        Long siteId = createSite(session.token(), "Retry Site", "https://retry.example");

        when(wpClient.publishPost(any(Site.class), any(Post.class), eq("publish")))
                .thenReturn(WpPublishResult.failure(503, "wp unavailable", true));

        mockMvc.perform(post("/publish")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "siteIds": [%d]
                                }
                                """.formatted(postId, siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[0].status").value("PENDING"));

        waitForPublishStatus(session.token(), "FAILED", Duration.ofSeconds(3));
        Integer retryCount = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM post_publish WHERE tenant_id = ?",
                Integer.class,
                session.tenantId()
        );
        org.junit.jupiter.api.Assertions.assertEquals(3, retryCount);
    }

    @Test
    void shouldRetryPublishAndEventuallySucceed() throws Exception {
        LoginSession session = ensureAndLogin("eventual_publish_admin", "admin123", "Eventual Publish Tenant");
        Long postId = createPost(session.token(), "Eventual title", "Eventual content body");
        Long siteId = createSite(session.token(), "Eventual Site", "https://eventual.example");

        when(wpClient.publishPost(any(Site.class), any(Post.class), eq("publish")))
                .thenReturn(
                        WpPublishResult.failure(503, "temporary error", true),
                        WpPublishResult.failure(503, "temporary error", true),
                        WpPublishResult.success(201, 202L, "https://eventual.example/?p=202", "{\"id\":202}")
                );

        mockMvc.perform(post("/publish")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "siteIds": [%d]
                                }
                                """.formatted(postId, siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[0].status").value("PENDING"));

        waitForPublishStatus(session.token(), "SUCCESS", Duration.ofSeconds(3));
        Integer retryCount = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM post_publish WHERE tenant_id = ?",
                Integer.class,
                session.tenantId()
        );
        org.junit.jupiter.api.Assertions.assertEquals(2, retryCount);
    }

    @Test
    void shouldRejectPublishingToAnotherTenantSite() throws Exception {
        LoginSession owner = ensureAndLogin("publish_owner", "admin123", "Owner Publish Tenant");
        LoginSession stranger = ensureAndLogin("publish_stranger", "admin123", "Stranger Publish Tenant");
        Long postId = createPost(owner.token(), "Owner title", "Owner content body");
        Long siteId = createSite(stranger.token(), "Stranger Site", "https://stranger.example");

        mockMvc.perform(post("/publish")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "siteIds": [%d]
                                }
                                """.formatted(postId, siteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
    }

    @Test
    void shouldListPublishRecordsWithinTenant() throws Exception {
        LoginSession owner = ensureAndLogin("publish_list_owner", "admin123", "Publish List Owner");
        LoginSession stranger = ensureAndLogin("publish_list_stranger", "admin123", "Publish List Stranger");
        Long postId = createPost(owner.token(), "List title", "List content body");
        Long siteId = createSite(owner.token(), "List Site", "https://list.example");

        when(wpClient.publishPost(any(Site.class), any(Post.class), eq("publish")))
                .thenReturn(WpPublishResult.success(201, 301L, "https://list.example/?p=301", "{\"id\":301}"));

        mockMvc.perform(post("/publish")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postId": %d,
                                  "siteIds": [%d]
                                }
                                """.formatted(postId, siteId)))
                .andExpect(status().isOk());

        waitForPublishStatus(owner.token(), "SUCCESS", Duration.ofSeconds(3));

        mockMvc.perform(get("/publish/list")
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].postId").value(postId))
                .andExpect(jsonPath("$.data[0].postTitle").value("List title"))
                .andExpect(jsonPath("$.data[0].siteId").value(siteId))
                .andExpect(jsonPath("$.data[0].siteName").value("List Site"))
                .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));

        mockMvc.perform(get("/publish/list")
                        .header("Authorization", "Bearer " + stranger.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private Long createPost(String token, String title, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/post/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "content": "%s"
                                }
                                """.formatted(title, content)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private Long createSite(String token, String name, String baseUrl) throws Exception {
        when(wpClient.testConnection(any(Site.class)))
                .thenReturn(new WpConnectionResult(true, "Connection successful"));

        MvcResult result = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "baseUrl": "%s",
                                  "wpUsername": "admin",
                                  "appPassword": "secret123"
                                }
                                """.formatted(name, baseUrl)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private void waitForPublishStatus(String token, String expectedStatus, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            MvcResult result = mockMvc.perform(get("/publish/list")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();

            String status = objectMapper.readTree(result.getResponse().getContentAsString())
                    .path("data")
                    .path(0)
                    .path("status")
                    .asText();
            if (expectedStatus.equals(status)) {
                return;
            }
            Thread.sleep(100);
        }
        org.junit.jupiter.api.Assertions.fail("Timed out waiting for publish status " + expectedStatus);
    }
}
