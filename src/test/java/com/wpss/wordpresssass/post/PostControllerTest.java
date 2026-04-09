package com.wpss.wordpresssass.post;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldCreateAndListPostsWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/post/create")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "First Post",
                                  "content": "Hello WordPress SaaS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("First Post"))
                .andExpect(jsonPath("$.data.tenantId").value(session.tenantId()));

        mockMvc.perform(get("/post/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("First Post"));
    }

    @Test
    void shouldIsolatePostsByTenant() throws Exception {
        LoginSession tenantA = ensureAndLogin("tenantA", "admin123", "Tenant A");
        LoginSession tenantB = ensureAndLogin("tenantB", "admin123", "Tenant B");

        mockMvc.perform(post("/post/create")
                        .header("Authorization", "Bearer " + tenantA.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Tenant 2001 Post",
                                  "content": "Only visible to tenant 2001"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/post/list")
                        .header("Authorization", "Bearer " + tenantB.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void shouldRejectRequestWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/post/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing Authorization header"));
    }
}
