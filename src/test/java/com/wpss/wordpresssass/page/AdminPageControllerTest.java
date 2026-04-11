package com.wpss.wordpresssass.page;

import com.wpss.wordpresssass.AuthTestSupport;
import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageLayoutVersionRepository;
import com.wpss.wordpresssass.page.domain.PageRepository;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPageControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageLayoutVersionRepository pageLayoutVersionRepository;

    @Autowired
    private SiteHomepageConfigRepository siteHomepageConfigRepository;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldListPagesAndLoadEditor() throws Exception {
        LoginSession session = ensureAndLogin("page_editor_admin", "admin123", "Page Editor Tenant");
        Long siteId = createSite(session, "Editor Site", "https://editor.example");

        mockMvc.perform(get("/api/admin/sites/" + siteId + "/pages")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].pageKey").value("HOME"))
                .andExpect(jsonPath("$.data[1].pageKey").value("PRODUCT"))
                .andExpect(jsonPath("$.data[2].pageKey").value("CHECKOUT"))
                .andExpect(jsonPath("$.data[3].pageKey").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));

        mockMvc.perform(get("/api/admin/sites/" + siteId + "/pages/HOME/editor")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageKey").value("HOME"))
                .andExpect(jsonPath("$.data.layout.pageKey").value("HOME"))
                .andExpect(jsonPath("$.data.layout.sections.length()").value(3))
                .andExpect(jsonPath("$.data.blockLibrary.length()").value(4));

        mockMvc.perform(get("/api/admin/sites/" + siteId + "/pages/PRODUCT/editor")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageKey").value("PRODUCT"))
                .andExpect(jsonPath("$.data.layout.pageKey").value("PRODUCT"))
                .andExpect(jsonPath("$.data.layout.sections.length()").value(3))
                .andExpect(jsonPath("$.data.blockLibrary.length()").value(2));
    }

    @Test
    void shouldCreateDraftVersionWhenSavingDraft() throws Exception {
        LoginSession session = ensureAndLogin("page_draft_admin", "admin123", "Page Draft Tenant");
        Long siteId = createSite(session, "Draft Site", "https://draft.example");

        Page homePageBefore = pageRepository.findBySiteAndPageKey(session.tenantId(), siteId, Page.HOME_PAGE_KEY)
                .orElseThrow();

        mockMvc.perform(put("/api/admin/sites/" + siteId + "/pages/HOME/draft")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "layout": {
                                    "pageKey": "HOME",
                                    "sections": [
                                      {
                                        "id": "hero-1",
                                        "type": "hero-banner",
                                        "props": {
                                          "title": "Updated Home Banner",
                                          "subtitle": "Draft subtitle"
                                        }
                                      },
                                      {
                                        "id": "menu-1",
                                        "type": "top-menu",
                                        "props": {
                                          "items": [
                                            {"label": "Home", "path": "/"}
                                          ]
                                        }
                                      },
                                      {
                                        "id": "featured-1",
                                        "type": "featured-products",
                                        "bindings": {
                                          "productIds": []
                                        }
                                      }
                                    ]
                                  },
                                  "versionNote": "First draft update"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageKey").value("HOME"))
                .andExpect(jsonPath("$.data.currentVersionStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.layout.sections[0].props.title").value("Updated Home Banner"));

        Page homePageAfter = pageRepository.findBySiteAndPageKey(session.tenantId(), siteId, Page.HOME_PAGE_KEY)
                .orElseThrow();
        assertThat(homePageAfter.getCurrentVersionId()).isNotEqualTo(homePageBefore.getPublishedVersionId());
        assertThat(homePageAfter.getPublishedVersionId()).isEqualTo(homePageBefore.getPublishedVersionId());
    }

    @Test
    void shouldPreviewAndPublishHomePage() throws Exception {
        LoginSession session = ensureAndLogin("page_publish_admin", "admin123", "Page Publish Tenant");
        Long siteId = createSite(session, "Publish Site", "https://publish.example");

        mockMvc.perform(put("/api/admin/sites/" + siteId + "/pages/HOME/draft")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "layout": {
                                    "pageKey": "HOME",
                                    "sections": [
                                      {
                                        "id": "hero-1",
                                        "type": "hero-banner",
                                        "props": {
                                          "title": "Preview Banner",
                                          "subtitle": "Published subtitle",
                                          "themeColor": "#0F766E"
                                        }
                                      },
                                      {
                                        "id": "menu-1",
                                        "type": "top-menu",
                                        "props": {
                                          "items": [
                                            {"label": "Home", "path": "/"},
                                            {"label": "Catalog", "path": "/category/all"}
                                          ]
                                        }
                                      },
                                      {
                                        "id": "featured-1",
                                        "type": "featured-products",
                                        "bindings": {
                                          "productIds": ["11", "12"]
                                        }
                                      }
                                    ]
                                  },
                                  "versionNote": "Ready to publish"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/sites/" + siteId + "/pages/HOME/preview")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageKey").value("HOME"))
                .andExpect(jsonPath("$.data.runtimeConfig.bannerTitle").value("Preview Banner"))
                .andExpect(jsonPath("$.data.runtimeConfig.themeColor").value("#0F766E"))
                .andExpect(jsonPath("$.data.runtimeConfig.featuredProductIds[0]").value("11"));

        mockMvc.perform(post("/api/admin/sites/" + siteId + "/pages/HOME/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.versionStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.runtimeConfig.bannerTitle").value("Preview Banner"));

        String runtimeConfigJson = siteHomepageConfigRepository.findBySite(session.tenantId(), siteId)
                .orElseThrow()
                .getConfigJson();
        assertThat(objectMapper.readTree(runtimeConfigJson).path("bannerTitle").asText()).isEqualTo("Preview Banner");
        assertThat(objectMapper.readTree(runtimeConfigJson).path("featuredProductIds").get(1).asText()).isEqualTo("12");
    }

    @Test
    void shouldListVersionsAndRollbackPublishedProductPage() throws Exception {
        LoginSession session = ensureAndLogin("page_history_admin", "admin123", "Page History Tenant");
        Long siteId = createSite(session, "History Site", "https://history.example");

        Page productPage = pageRepository.findBySiteAndPageKey(session.tenantId(), siteId, Page.PRODUCT_PAGE_KEY)
                .orElseThrow();
        Long publishedVersionId = productPage.getPublishedVersionId();

        mockMvc.perform(put("/api/admin/sites/" + siteId + "/pages/PRODUCT/draft")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "layout": {
                                    "pageKey": "PRODUCT",
                                    "sections": [
                                      {
                                        "id": "product-copy-1",
                                        "type": "rich-text",
                                        "props": {
                                          "title": "New product promise",
                                          "body": "Updated product positioning."
                                        }
                                      },
                                      {
                                        "id": "trust-1",
                                        "type": "trust-badges",
                                        "props": {
                                          "items": ["Fast shipping", "Secure checkout"]
                                        }
                                      },
                                      {
                                        "id": "shipping-1",
                                        "type": "rich-text",
                                        "props": {
                                          "title": "Support",
                                          "body": "Updated shipping promise."
                                        }
                                      }
                                    ]
                                  },
                                  "versionNote": "Product detail refresh"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageKey").value("PRODUCT"))
                .andExpect(jsonPath("$.data.currentVersionStatus").value("DRAFT"));

        mockMvc.perform(get("/api/admin/sites/" + siteId + "/pages/PRODUCT/versions")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].currentVersion").value(true))
                .andExpect(jsonPath("$.data[1].publishedVersion").value(true));

        mockMvc.perform(post("/api/admin/sites/" + siteId + "/pages/PRODUCT/versions/" + publishedVersionId + "/rollback")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "versionNote": "Rollback to baseline"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageKey").value("PRODUCT"))
                .andExpect(jsonPath("$.data.currentVersionStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.layout.sections[0].props.title").value("Product detail promise"));

        Page productPageAfter = pageRepository.findBySiteAndPageKey(session.tenantId(), siteId, Page.PRODUCT_PAGE_KEY)
                .orElseThrow();
        assertThat(productPageAfter.getCurrentVersionId()).isNotEqualTo(productPageAfter.getPublishedVersionId());
        assertThat(pageLayoutVersionRepository.findByPage(session.tenantId(), siteId, productPage.getId())).hasSize(3);
    }

    @Test
    void shouldRejectReadingAnotherTenantPage() throws Exception {
        LoginSession owner = ensureAndLogin("page_owner", "admin123", "Page Owner");
        LoginSession stranger = ensureAndLogin("page_stranger", "admin123", "Page Stranger");
        Long siteId = createSite(owner, "Owner Page Site", "https://page-owner.example");

        mockMvc.perform(get("/api/admin/sites/" + siteId + "/pages")
                        .header("Authorization", "Bearer " + stranger.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
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
