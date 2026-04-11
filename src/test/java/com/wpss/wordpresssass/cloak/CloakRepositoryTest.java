package com.wpss.wordpresssass.cloak;

import com.wpss.wordpresssass.AuthTestSupport;
import com.wpss.wordpresssass.cloak.domain.CloakHitLog;
import com.wpss.wordpresssass.cloak.domain.CloakHitLogRepository;
import com.wpss.wordpresssass.cloak.domain.CloakMatchMode;
import com.wpss.wordpresssass.cloak.domain.CloakResultType;
import com.wpss.wordpresssass.cloak.domain.CloakRule;
import com.wpss.wordpresssass.cloak.domain.CloakRuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CloakRepositoryTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private CloakRuleRepository cloakRuleRepository;

    @Autowired
    private CloakHitLogRepository cloakHitLogRepository;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldSaveAndQueryCloakRuleAndHitLog() throws Exception {
        LoginSession session = ensureAndLogin("cloak_repo_admin", "admin123", "Cloak Repo Tenant");
        Long siteId = createSite(session, "Cloak Site", "https://cloak.example");

        CloakRule savedRule = cloakRuleRepository.save(CloakRule.createDraft(
                session.tenantId(),
                siteId,
                "Facebook Review Gate",
                10,
                CloakMatchMode.ALL,
                100,
                """
                        [{"dimension":"COUNTRY","operator":"IN","values":["US"]}]
                        """,
                CloakResultType.REVIEW_PAGE,
                """
                        {"pageKey":"HOME","reason":"review-traffic"}
                        """,
                "system"
        ));

        assertThat(savedRule.getId()).isNotNull();
        assertThat(cloakRuleRepository.findBySite(session.tenantId(), siteId))
                .extracting(CloakRule::getRuleName)
                .containsExactly("Facebook Review Gate");

        CloakHitLog savedHitLog = cloakHitLogRepository.save(CloakHitLog.record(
                session.tenantId(),
                siteId,
                savedRule.getId(),
                CloakResultType.REVIEW_PAGE,
                "req-1001",
                """
                        {"country":"US","referer":"facebook.com","deviceType":"MOBILE"}
                        """,
                """
                        [{"dimension":"COUNTRY","operator":"IN","values":["US"]}]
                        """
        ));

        assertThat(savedHitLog.getId()).isNotNull();
        assertThat(cloakHitLogRepository.findBySite(session.tenantId(), siteId, 10))
                .hasSize(1)
                .extracting(CloakHitLog::getDecision)
                .containsExactly(CloakResultType.REVIEW_PAGE);
    }

    @Test
    void shouldIsolateCloakRulesByTenant() throws Exception {
        LoginSession owner = ensureAndLogin("cloak_owner", "admin123", "Cloak Owner");
        LoginSession stranger = ensureAndLogin("cloak_stranger", "admin123", "Cloak Stranger");
        Long ownerSiteId = createSite(owner, "Owner Cloak Site", "https://owner-cloak.example");

        cloakRuleRepository.save(CloakRule.createDraft(
                owner.tenantId(),
                ownerSiteId,
                "Owner Rule",
                1,
                CloakMatchMode.ALL,
                100,
                """
                        [{"dimension":"UTM_SOURCE","operator":"IN","values":["fb"]}]
                        """,
                CloakResultType.NORMAL_PAGE,
                """
                        {"pageKey":"HOME","reason":"default"}
                        """,
                "system"
        ));

        assertThat(cloakRuleRepository.findBySite(owner.tenantId(), ownerSiteId)).hasSize(1);
        assertThat(cloakRuleRepository.findBySite(stranger.tenantId(), ownerSiteId)).isEmpty();
    }

    private Long createSite(LoginSession session, String name, String baseUrl) throws Exception {
        String response = mockMvc.perform(post("/api/admin/sites")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "baseUrl": "%s",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """.formatted(name, baseUrl)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }
}
