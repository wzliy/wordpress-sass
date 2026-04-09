package com.wpss.wordpresssass.site;

import com.wpss.wordpresssass.AuthTestSupport;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpClient;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpConnectionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SiteControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WpClient wpClient;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldAddAndListSitesWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "My Blog",
                                  "baseUrl": "https://example.com/",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("My Blog"))
                .andExpect(jsonPath("$.data.siteType").value("REGISTERED"))
                .andExpect(jsonPath("$.data.baseUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data.tenantId").value(session.tenantId()));

        mockMvc.perform(get("/site/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("My Blog"))
                .andExpect(jsonPath("$.data[0].provisionStatus").value("NONE"));
    }

    @Test
    void shouldTestSiteConnection() throws Exception {
        LoginSession session = ensureAndLogin("site_admin", "admin123", "Site Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "WP Site",
                                  "baseUrl": "https://wordpress.test",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long siteId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        when(wpClient.testConnection(any(Site.class)))
                .thenReturn(new WpConnectionResult(true, "Connection successful"));

        mockMvc.perform(get("/site/test")
                        .header("Authorization", "Bearer " + session.token())
                        .param("id", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(siteId))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Connection successful"));
    }

    @Test
    void shouldRejectTestingAnotherTenantSite() throws Exception {
        LoginSession owner = ensureAndLogin("owner_admin", "admin123", "Owner Tenant");
        LoginSession stranger = ensureAndLogin("stranger_admin", "admin123", "Stranger Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tenant Site",
                                  "baseUrl": "https://tenant.example",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long siteId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/site/test")
                        .header("Authorization", "Bearer " + stranger.token())
                        .param("id", String.valueOf(siteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
    }

    @Test
    void shouldProvisionSite() throws Exception {
        LoginSession session = ensureAndLogin("provision_admin", "admin123", "Provision Tenant");

        mockMvc.perform(post("/site/provision")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Provisioned Blog",
                                  "adminEmail": "owner@example.com",
                                  "subdomainPrefix": "tenant-blog"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.siteId").exists())
                .andExpect(jsonPath("$.data.domain").value("https://tenant-blog.wp.local"))
                .andExpect(jsonPath("$.data.adminUrl").value("https://tenant-blog.wp.local/wp-admin"))
                .andExpect(jsonPath("$.data.provisionStatus").value("ACTIVE"));

        mockMvc.perform(get("/site/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].siteType").value("PROVISIONED"))
                .andExpect(jsonPath("$.data[0].provisionStatus").value("ACTIVE"));
    }
}
