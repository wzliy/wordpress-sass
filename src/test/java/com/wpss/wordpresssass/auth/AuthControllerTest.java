package com.wpss.wordpresssass.auth;

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
class AuthControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldLoginWithBootstrapAdmin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.tenantId").isNumber())
                .andExpect(jsonPath("$.data.expiresAt").isNumber())
                .andExpect(jsonPath("$.data.expireSeconds").isNumber());
    }

    @Test
    void shouldIssueStandardJwtToken() throws Exception {
        String token = loginDefaultAdmin().token();
        org.junit.jupiter.api.Assertions.assertEquals(3, token.split("\\.").length);
    }

    @Test
    void shouldReadCurrentUserFromToken() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.tenantId").value(session.tenantId()))
                .andExpect(jsonPath("$.data.expiresAt").isEmpty())
                .andExpect(jsonPath("$.data.expireSeconds").isNumber());
    }

    @Test
    void shouldRejectInvalidTokenAsUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
}
