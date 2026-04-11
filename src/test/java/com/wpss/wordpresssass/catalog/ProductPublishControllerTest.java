package com.wpss.wordpresssass.catalog;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductPublishControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldPublishProductToSpecificSiteAndListPerSiteStatus() throws Exception {
        LoginSession session = ensureAndLogin("product_publish_single", "admin123", "Product Publish Single Tenant");
        Long siteA = createSite(session, "North Publish Site", "https://north-publish.example");
        Long siteB = createSite(session, "South Publish Site", "https://south-publish.example");
        Long categoryId = createCategory(session, "Shoes", "shoes");
        Long productId = createProduct(session, categoryId, "SKU-PUB-1", "Publish Product");

        mockMvc.perform(get("/api/admin/products/" + productId + "/publishes")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].publishStatus").value("UNPUBLISHED"))
                .andExpect(jsonPath("$.data[1].publishStatus").value("UNPUBLISHED"));

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteA + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(siteA))
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.publishStatus").value("PUBLISHED"));

        mockMvc.perform(get("/api/admin/products/" + productId + "/publishes")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.siteId==" + siteA + ")].publishStatus").value(hasItem("PUBLISHED")))
                .andExpect(jsonPath("$.data[?(@.siteId==" + siteB + ")].publishStatus").value(hasItem("UNPUBLISHED")));

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteA + "/unpublish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteId").value(siteA))
                .andExpect(jsonPath("$.data.publishStatus").value("UNPUBLISHED"));
    }

    @Test
    void shouldPublishAndUnpublishProductAcrossAllSites() throws Exception {
        LoginSession session = ensureAndLogin("product_publish_all", "admin123", "Product Publish All Tenant");
        createSite(session, "Global Publish A", "https://global-publish-a.example");
        createSite(session, "Global Publish B", "https://global-publish-b.example");
        Long categoryId = createCategory(session, "Bags", "bags");
        Long productId = createProduct(session, categoryId, "SKU-PUB-ALL", "Global Publish Product");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/publish-all")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].publishStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.data[1].publishStatus").value("PUBLISHED"));

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/unpublish-all")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].publishStatus").value("UNPUBLISHED"))
                .andExpect(jsonPath("$.data[1].publishStatus").value("UNPUBLISHED"));
    }

    @Test
    void shouldRejectPublishingAnotherTenantProduct() throws Exception {
        LoginSession owner = ensureAndLogin("product_publish_owner", "admin123", "Product Publish Owner Tenant");
        LoginSession stranger = ensureAndLogin("product_publish_stranger", "admin123", "Product Publish Stranger Tenant");
        Long siteId = createSite(owner, "Owner Publish Site", "https://owner-publish.example");
        Long categoryId = createCategory(owner, "Owner Category", "owner-category");
        Long productId = createProduct(owner, categoryId, "SKU-OWNER-PUB", "Owner Publish Product");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + stranger.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void shouldRejectPublishingToMissingSite() throws Exception {
        LoginSession session = ensureAndLogin("product_publish_missing_site", "admin123", "Product Publish Missing Site Tenant");
        Long categoryId = createCategory(session, "Accessories", "accessories");
        Long productId = createProduct(session, categoryId, "SKU-MISS-SITE", "Missing Site Product");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/999999/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
    }

    private Long createSite(LoginSession session, String name, String baseUrl) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/admin/sites")
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

        return objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private Long createCategory(LoginSession session, String name, String slug) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "slug": "%s"
                                }
                                """.formatted(name, slug)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private Long createProduct(LoginSession session, Long categoryId, String sku, String title) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "%s",
                                  "title": "%s",
                                  "categoryId": %d,
                                  "price": 49.90
                                }
                                """.formatted(sku, title, categoryId)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }
}
