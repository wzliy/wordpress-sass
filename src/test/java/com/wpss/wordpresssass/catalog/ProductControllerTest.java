package com.wpss.wordpresssass.catalog;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldCreateAndListProductsWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();
        Long categoryId = createCategory(session, "Bags", "bags");

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "sku-001",
                                  "title": "Travel Backpack 40L",
                                  "categoryId": %d,
                                  "coverImage": "https://cdn.example.com/backpack-cover.jpg",
                                  "galleryImages": [
                                    "https://cdn.example.com/backpack-1.jpg",
                                    "https://cdn.example.com/backpack-2.jpg"
                                  ],
                                  "descriptionHtml": "<p>Water-resistant carry-on backpack.</p>",
                                  "sizes": ["20L", "40L"],
                                  "price": 59.90,
                                  "compareAtPrice": 79.90
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-001"))
                .andExpect(jsonPath("$.data.title").value("Travel Backpack 40L"))
                .andExpect(jsonPath("$.data.categoryId").value(categoryId))
                .andExpect(jsonPath("$.data.categoryName").value("Bags"))
                .andExpect(jsonPath("$.data.galleryImages.length()").value(2))
                .andExpect(jsonPath("$.data.sizes[1]").value("40L"))
                .andExpect(jsonPath("$.data.price").value(59.9))
                .andExpect(jsonPath("$.data.compareAtPrice").value(79.9))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$.data[0].categoryName").value("Bags"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        LoginSession session = ensureAndLogin("product_update", "admin123", "Product Update Tenant");
        Long bagsId = createCategory(session, "Bags", "bags");
        Long outdoorId = createCategory(session, "Outdoor", "outdoor");
        Long productId = createProduct(session, bagsId, "SKU-100", "Original Product");

        mockMvc.perform(put("/api/admin/products/" + productId)
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "sku-200",
                                  "title": "Updated Outdoor Pack",
                                  "categoryId": %d,
                                  "coverImage": "https://cdn.example.com/outdoor-cover.jpg",
                                  "galleryImages": ["https://cdn.example.com/outdoor-1.jpg"],
                                  "descriptionHtml": "<p>Updated description</p>",
                                  "sizes": ["M", "L"],
                                  "price": 129.90,
                                  "compareAtPrice": 159.90,
                                  "status": "ACTIVE"
                                }
                                """.formatted(outdoorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.sku").value("SKU-200"))
                .andExpect(jsonPath("$.data.title").value("Updated Outdoor Pack"))
                .andExpect(jsonPath("$.data.categoryId").value(outdoorId))
                .andExpect(jsonPath("$.data.categoryName").value("Outdoor"))
                .andExpect(jsonPath("$.data.coverImage").value("https://cdn.example.com/outdoor-cover.jpg"))
                .andExpect(jsonPath("$.data.galleryImages.length()").value(1))
                .andExpect(jsonPath("$.data.sizes[0]").value("M"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldActivateAndDeactivateProduct() throws Exception {
        LoginSession session = ensureAndLogin("product_toggle", "admin123", "Product Toggle Tenant");
        Long categoryId = createCategory(session, "Accessories", "accessories");
        Long productId = createProduct(session, categoryId, "SKU-300", "Toggle Product");

        mockMvc.perform(post("/api/admin/products/" + productId + "/activate")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/api/admin/products/" + productId + "/deactivate")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void shouldRejectDuplicateSkuWithinTenant() throws Exception {
        LoginSession session = ensureAndLogin("product_duplicate", "admin123", "Product Duplicate Tenant");
        Long categoryId = createCategory(session, "Shoes", "shoes");

        createProduct(session, categoryId, "SKU-DUP", "Original Product");

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "sku-dup",
                                  "title": "Duplicate Product",
                                  "categoryId": %d,
                                  "price": 89.90
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product SKU already exists"));
    }

    @Test
    void shouldRejectProductWhenCategoryMissing() throws Exception {
        LoginSession session = ensureAndLogin("product_missing_category", "admin123", "Product Missing Category Tenant");

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-404",
                                  "title": "Missing Category Product",
                                  "categoryId": 999999,
                                  "price": 49.90
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void shouldRejectUpdatingAnotherTenantProduct() throws Exception {
        LoginSession owner = ensureAndLogin("product_owner", "admin123", "Product Owner Tenant");
        LoginSession stranger = ensureAndLogin("product_stranger", "admin123", "Product Stranger Tenant");
        Long categoryId = createCategory(owner, "Owner Category", "owner-category");
        Long productId = createProduct(owner, categoryId, "SKU-OWNER", "Owner Product");

        mockMvc.perform(put("/api/admin/products/" + productId)
                        .header("Authorization", "Bearer " + stranger.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-HIJACK",
                                  "title": "Hijacked Product",
                                  "categoryId": %d,
                                  "price": 99.90
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"));
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
                                  "price": 39.90
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
