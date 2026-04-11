package com.wpss.wordpresssass.site;

import com.wpss.wordpresssass.AuthTestSupport;
import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageLayoutVersionRepository;
import com.wpss.wordpresssass.page.domain.PageRepository;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private SiteHomepageConfigRepository siteHomepageConfigRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageLayoutVersionRepository pageLayoutVersionRepository;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldAddAndListSitesWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        MvcResult addResult = mockMvc.perform(post("/site/add")
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
                .andExpect(jsonPath("$.data.siteCode").exists())
                .andExpect(jsonPath("$.data.siteType").value("REGISTERED"))
                .andExpect(jsonPath("$.data.baseUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data.themeColor").value("#2563EB"))
                .andExpect(jsonPath("$.data.bannerTitle").value("My Blog"))
                .andExpect(jsonPath("$.data.bannerSubtitle").value("Your storefront is ready to be customized."))
                .andExpect(jsonPath("$.data.tenantId").value(session.tenantId()))
                .andReturn();

        Long siteId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/site/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("My Blog"))
                .andExpect(jsonPath("$.data[0].siteCode").exists())
                .andExpect(jsonPath("$.data[0].provisionStatus").value("NONE"));

        mockMvc.perform(get("/api/admin/domains")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].domain").value("example.com"))
                .andExpect(jsonPath("$.data[0].primary").value(true));

        String homepageConfigJson = siteHomepageConfigRepository.findBySite(session.tenantId(), siteId)
                .orElseThrow()
                .getConfigJson();
        var homepageConfig = objectMapper.readTree(homepageConfigJson);
        assertThat(homepageConfig.path("bannerTitle").asText()).isEqualTo("My Blog");
        assertThat(homepageConfig.path("menuItems").size()).isEqualTo(3);

        Page homePage = pageRepository.findBySiteAndPageKey(session.tenantId(), siteId, Page.HOME_PAGE_KEY)
                .orElseThrow();
        assertThat(homePage.getPublishedVersionId()).isNotNull();
        assertThat(pageRepository.findBySite(session.tenantId(), siteId))
                .extracting(Page::getPageKey)
                .containsExactly(
                        Page.HOME_PAGE_KEY,
                        Page.PRODUCT_PAGE_KEY,
                        Page.CHECKOUT_PAGE_KEY,
                        Page.SUCCESS_PAGE_KEY
                );

        var versions = pageLayoutVersionRepository.findByPage(session.tenantId(), siteId, homePage.getId());
        assertThat(versions).hasSize(1);
        assertThat(objectMapper.readTree(versions.get(0).getLayoutJson()).path("sections")).hasSize(3);
    }

    @Test
    void shouldTestSiteConnection() throws Exception {
        LoginSession session = ensureAndLogin("site_admin", "admin123", "Site Tenant");

        MvcResult addResult = mockMvc.perform(post("/api/admin/sites")
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
                .andExpect(jsonPath("$.data.siteCode").exists())
                .andExpect(jsonPath("$.data.themeColor").value("#2563EB"))
                .andExpect(jsonPath("$.data.bannerTitle").value("WP Site"))
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

        MvcResult provisionResult = mockMvc.perform(post("/site/provision")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Provisioned Blog",
                                  "adminEmail": "owner@example.com",
                                  "templateCode": "starter-one-product",
                                  "countryCode": "US",
                                  "languageCode": "en",
                                  "currencyCode": "USD",
                                  "subdomainPrefix": "tenant-blog"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.siteId").exists())
                .andExpect(jsonPath("$.data.domain").value("https://tenant-blog.wp.local"))
                .andExpect(jsonPath("$.data.adminUrl").value("https://tenant-blog.wp.local/wp-admin"))
                .andExpect(jsonPath("$.data.provisionStatus").value("ACTIVE"))
                .andReturn();

        Long siteId = objectMapper.readTree(provisionResult.getResponse().getContentAsString())
                .path("data")
                .path("siteId")
                .asLong();

        mockMvc.perform(get("/site/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].siteType").value("PROVISIONED"))
                .andExpect(jsonPath("$.data[0].provisionStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].siteCode").exists())
                .andExpect(jsonPath("$.data[0].themeColor").value("#2563EB"))
                .andExpect(jsonPath("$.data[0].bannerTitle").value("Provisioned Blog"))
                .andExpect(jsonPath("$.data[0].countryCode").value("US"))
                .andExpect(jsonPath("$.data[0].languageCode").value("en"))
                .andExpect(jsonPath("$.data[0].currencyCode").value("USD"));

        mockMvc.perform(get("/site/workspace")
                        .header("Authorization", "Bearer " + session.token())
                        .param("id", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.templateCode").value("starter-one-product"))
                .andExpect(jsonPath("$.data.profile.countryCode").value("US"))
                .andExpect(jsonPath("$.data.profile.languageCode").value("en"))
                .andExpect(jsonPath("$.data.profile.currencyCode").value("USD"))
                .andExpect(jsonPath("$.data.readiness.level").value("BASIC_READY"));

        String homepageConfigJson = siteHomepageConfigRepository.findBySite(session.tenantId(), siteId)
                .orElseThrow()
                .getConfigJson();
        var homepageConfig = objectMapper.readTree(homepageConfigJson);
        assertThat(homepageConfig.path("templateCode").asText()).isEqualTo("starter-one-product");
        assertThat(homepageConfig.path("themeColor").asText()).isEqualTo("#2563EB");

        Page homePage = pageRepository.findBySiteAndPageKey(session.tenantId(), siteId, Page.HOME_PAGE_KEY)
                .orElseThrow();
        assertThat(homePage.getPublishedVersionId()).isNotNull();
        assertThat(pageLayoutVersionRepository.findByPage(session.tenantId(), siteId, homePage.getId())).hasSize(1);
        assertThat(pageRepository.findBySite(session.tenantId(), siteId))
                .extracting(Page::getPageKey)
                .containsExactly(
                        Page.HOME_PAGE_KEY,
                        Page.PRODUCT_PAGE_KEY,
                        Page.CHECKOUT_PAGE_KEY,
                        Page.SUCCESS_PAGE_KEY
                );
    }

    @Test
    void shouldListBuiltInTemplates() throws Exception {
        LoginSession session = ensureAndLogin("template_admin", "admin123", "Template Tenant");

        mockMvc.perform(get("/site/template/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].code").exists())
                .andExpect(jsonPath("$.data[0].name").exists());
    }

    @Test
    void shouldRejectProvisionWhenTemplateMissing() throws Exception {
        LoginSession session = ensureAndLogin("template_missing", "admin123", "Template Missing Tenant");

        mockMvc.perform(post("/site/provision")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Provisioned Blog",
                                  "adminEmail": "owner@example.com",
                                  "templateCode": "unknown-template",
                                  "countryCode": "US",
                                  "languageCode": "en",
                                  "currencyCode": "USD",
                                  "subdomainPrefix": "tenant-blog"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Template not found"));
    }

    @Test
    void shouldReturnSiteWorkspaceSummary() throws Exception {
        LoginSession session = ensureAndLogin("workspace_admin", "admin123", "Workspace Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Workspace Site",
                                  "baseUrl": "https://workspace.example",
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

        mockMvc.perform(get("/site/workspace")
                        .header("Authorization", "Bearer " + session.token())
                        .param("id", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(siteId))
                .andExpect(jsonPath("$.data.tenantId").value(session.tenantId()))
                .andExpect(jsonPath("$.data.workspaceStatus").value("ACTION_REQUIRED"))
                .andExpect(jsonPath("$.data.profile.name").value("Workspace Site"))
                .andExpect(jsonPath("$.data.profile.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.readiness.level").value("BASIC_READY"))
                .andExpect(jsonPath("$.data.moduleSummaries.length()").value(5))
                .andExpect(jsonPath("$.data.pendingTasks.length()").value(0))
                .andExpect(jsonPath("$.data.quickActions.length()").value(5))
                .andExpect(jsonPath("$.data.quickActions[2].label").value("首页装修"));
    }

    @Test
    void shouldBindAndListAdditionalDomain() throws Exception {
        LoginSession session = ensureAndLogin("domain_admin", "admin123", "Domain Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Domain Site",
                                  "baseUrl": "https://domain.example",
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

        mockMvc.perform(post("/api/admin/domains")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "siteId": %d,
                                  "domain": "shop.domain.example",
                                  "primary": false
                                }
                                """.formatted(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(siteId))
                .andExpect(jsonPath("$.data.domain").value("shop.domain.example"))
                .andExpect(jsonPath("$.data.primary").value(false));

        mockMvc.perform(get("/api/admin/domains")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].domain").value("domain.example"))
                .andExpect(jsonPath("$.data[0].primary").value(true))
                .andExpect(jsonPath("$.data[1].domain").value("shop.domain.example"))
                .andExpect(jsonPath("$.data[1].primary").value(false));
    }

    @Test
    void shouldExposeAdminSiteListAndDetail() throws Exception {
        LoginSession session = ensureAndLogin("admin_site_api", "admin123", "Admin Site API Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Admin Detail Site",
                                  "baseUrl": "https://detail.example",
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

        mockMvc.perform(get("/api/admin/sites")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(siteId))
                .andExpect(jsonPath("$.data[0].name").value("Admin Detail Site"))
                .andExpect(jsonPath("$.data[0].siteCode").exists())
                .andExpect(jsonPath("$.data[0].bannerSubtitle").value("Your storefront is ready to be customized."));

        mockMvc.perform(get("/api/admin/sites/" + siteId)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.site.id").value(siteId))
                .andExpect(jsonPath("$.data.site.name").value("Admin Detail Site"))
                .andExpect(jsonPath("$.data.site.siteCode").exists())
                .andExpect(jsonPath("$.data.site.themeColor").value("#2563EB"))
                .andExpect(jsonPath("$.data.site.bannerTitle").value("Admin Detail Site"))
                .andExpect(jsonPath("$.data.domains.length()").value(1))
                .andExpect(jsonPath("$.data.domains[0].domain").value("detail.example"));
    }

    @Test
    void shouldSwitchPrimaryDomainWhenBindingPrimaryDomain() throws Exception {
        LoginSession session = ensureAndLogin("domain_primary", "admin123", "Primary Domain Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Primary Site",
                                  "baseUrl": "https://primary.example",
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

        mockMvc.perform(post("/api/admin/domains")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "siteId": %d,
                                  "domain": "brand.primary.example",
                                  "primary": true
                                }
                                """.formatted(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.primary").value(true));

        mockMvc.perform(get("/api/admin/domains")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].domain").value("brand.primary.example"))
                .andExpect(jsonPath("$.data[0].primary").value(true))
                .andExpect(jsonPath("$.data[1].domain").value("primary.example"))
                .andExpect(jsonPath("$.data[1].primary").value(false));
    }

    @Test
    void shouldDisableAndEnableSiteViaAdminApi() throws Exception {
        LoginSession session = ensureAndLogin("admin_site_toggle", "admin123", "Admin Site Toggle Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Toggle Site",
                                  "baseUrl": "https://toggle.example",
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

        mockMvc.perform(post("/api/admin/sites/" + siteId + "/disable")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.site.id").value(siteId))
                .andExpect(jsonPath("$.data.site.status").value(0))
                .andExpect(jsonPath("$.data.site.statusMessage").value("Site disabled"));

        mockMvc.perform(post("/api/admin/sites/" + siteId + "/enable")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.site.id").value(siteId))
                .andExpect(jsonPath("$.data.site.status").value(1))
                .andExpect(jsonPath("$.data.site.statusMessage").value("Site enabled"));
    }

    @Test
    void shouldRejectDuplicateDomainWhenRegisteringSite() throws Exception {
        LoginSession owner = ensureAndLogin("domain_owner", "admin123", "Domain Owner");
        LoginSession stranger = ensureAndLogin("domain_stranger", "admin123", "Domain Stranger");

        mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Owner Site",
                                  "baseUrl": "https://dup.example",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + stranger.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Stranger Site",
                                  "baseUrl": "https://dup.example",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Domain already bound"));
    }

    @Test
    void shouldRejectReadingAnotherTenantWorkspace() throws Exception {
        LoginSession owner = ensureAndLogin("workspace_owner", "admin123", "Workspace Owner");
        LoginSession stranger = ensureAndLogin("workspace_stranger", "admin123", "Workspace Stranger");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tenant Workspace Site",
                                  "baseUrl": "https://workspace-tenant.example",
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

        mockMvc.perform(get("/site/workspace")
                        .header("Authorization", "Bearer " + stranger.token())
                        .param("id", String.valueOf(siteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
    }
}
