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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldCreateAndListCategoriesWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Summer Dresses"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Summer Dresses"))
                .andExpect(jsonPath("$.data.slug").value("summer-dresses"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Summer Dresses"))
                .andExpect(jsonPath("$.data[0].slug").value("summer-dresses"));
    }

    @Test
    void shouldDisableAndEnableCategory() throws Exception {
        LoginSession session = ensureAndLogin("category_toggle", "admin123", "Category Toggle Tenant");

        MvcResult createResult = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Featured Picks",
                                  "slug": "featured-picks"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long categoryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/admin/categories/" + categoryId + "/disable")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/admin/categories/" + categoryId + "/enable")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectDuplicateSlugWithinTenant() throws Exception {
        LoginSession session = ensureAndLogin("category_duplicate", "admin123", "Category Duplicate Tenant");

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Landing",
                                  "slug": "landing"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Landing Repeat",
                                  "slug": "landing"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Category slug already exists"));
    }

    @Test
    void shouldRejectTogglingAnotherTenantCategory() throws Exception {
        LoginSession owner = ensureAndLogin("category_owner", "admin123", "Category Owner Tenant");
        LoginSession stranger = ensureAndLogin("category_stranger", "admin123", "Category Stranger Tenant");

        MvcResult createResult = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Owner Category",
                                  "slug": "owner-category"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long categoryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/admin/categories/" + categoryId + "/disable")
                        .header("Authorization", "Bearer " + stranger.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }
}
