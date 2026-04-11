package com.wpss.wordpresssass.site;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubsiteSettingsControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldReadAndUpdateSubsiteSettings() throws Exception {
        LoginSession session = ensureAndLogin("subsite_settings_owner", "admin123", "Subsite Settings Tenant");
        Long siteId = createSite(session, "Subsite Settings Site", "https://subsite-settings.example");

        mockMvc.perform(get("/api/subsite/settings")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(siteId))
                .andExpect(jsonPath("$.data.siteName").value("Subsite Settings Site"))
                .andExpect(jsonPath("$.data.siteUrl").value("https://subsite-settings.example"))
                .andExpect(jsonPath("$.data.bannerTitle").value("Subsite Settings Site"));

        mockMvc.perform(put("/api/subsite/settings")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "siteId": %d,
                                  "siteName": "Updated Subsite",
                                  "supportEmail": "support@subsite.example",
                                  "supportPhone": "+15550002222",
                                  "whatsapp": "+15550003333",
                                  "facebook": "https://facebook.com/subsite",
                                  "currencyCode": "USD",
                                  "countryCode": "US",
                                  "languageCode": "en",
                                  "logisticsText": "Ships within 48 hours.",
                                  "logoUrl": "https://cdn.example.com/subsite-logo.svg",
                                  "bannerTitle": "Updated Banner",
                                  "bannerSubtitle": "Support your current offers."
                                }
                                """.formatted(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteName").value("Updated Subsite"))
                .andExpect(jsonPath("$.data.supportEmail").value("support@subsite.example"))
                .andExpect(jsonPath("$.data.supportPhone").value("+15550002222"))
                .andExpect(jsonPath("$.data.whatsapp").value("+15550003333"))
                .andExpect(jsonPath("$.data.facebook").value("https://facebook.com/subsite"))
                .andExpect(jsonPath("$.data.currencyCode").value("USD"))
                .andExpect(jsonPath("$.data.countryCode").value("US"))
                .andExpect(jsonPath("$.data.languageCode").value("en"))
                .andExpect(jsonPath("$.data.logisticsText").value("Ships within 48 hours."))
                .andExpect(jsonPath("$.data.logoUrl").value("https://cdn.example.com/subsite-logo.svg"))
                .andExpect(jsonPath("$.data.bannerTitle").value("Updated Banner"))
                .andExpect(jsonPath("$.data.bannerSubtitle").value("Support your current offers."));

        String siteName = jdbcTemplate.queryForObject("SELECT name FROM site WHERE tenant_id = ? AND id = ?", String.class, session.tenantId(), siteId);
        String logoUrl = jdbcTemplate.queryForObject("SELECT logo_url FROM site WHERE tenant_id = ? AND id = ?", String.class, session.tenantId(), siteId);
        String bannerTitle = jdbcTemplate.queryForObject("SELECT banner_title FROM site WHERE tenant_id = ? AND id = ?", String.class, session.tenantId(), siteId);
        String defaultConfigJson = jdbcTemplate.queryForObject(
                "SELECT default_config_json FROM site_setting WHERE tenant_id = ? AND site_id = ?",
                String.class,
                session.tenantId(),
                siteId
        );

        assertThat(siteName).isEqualTo("Updated Subsite");
        assertThat(logoUrl).isEqualTo("https://cdn.example.com/subsite-logo.svg");
        assertThat(bannerTitle).isEqualTo("Updated Banner");
        assertThat(defaultConfigJson).contains("support@subsite.example");
        assertThat(defaultConfigJson).contains("Ships within 48 hours.");
        assertThat(defaultConfigJson).contains("\"currencyCode\":\"USD\"");
    }

    @Test
    void shouldRejectAccessingAnotherTenantSubsiteSettings() throws Exception {
        LoginSession owner = ensureAndLogin("subsite_settings_owner_2", "admin123", "Subsite Settings Owner Tenant");
        LoginSession stranger = ensureAndLogin("subsite_settings_stranger", "admin123", "Subsite Settings Stranger Tenant");
        Long siteId = createSite(owner, "Private Subsite", "https://private-subsite.example");

        mockMvc.perform(get("/api/subsite/settings")
                        .header("Authorization", "Bearer " + stranger.token())
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
    }

    private Long createSite(LoginSession session, String name, String baseUrl) throws Exception {
        MvcResult addResult = mockMvc.perform(post("/api/admin/sites")
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
                .andReturn();

        return objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }
}
