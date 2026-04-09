package com.wpss.wordpresssass.user;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldCreateAndListUsersWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/users/create")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "editor01",
                                  "password": "admin123",
                                  "email": "editor01@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("editor01"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.nickname").value("editor01"))
                .andExpect(jsonPath("$.data.tenantId").value(session.tenantId()));

        mockMvc.perform(get("/users/list")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user WHERE tenant_id = ? AND username = ?",
                Integer.class,
                session.tenantId(),
                "editor01"
        );
        assertEquals(1, count);
    }

    @Test
    void shouldGetAndUpdateUserProfileWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/users/create")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "profile_user",
                                  "password": "admin123",
                                  "email": "profile_user@example.com"
                                }
                                """))
                .andExpect(status().isOk());

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM user WHERE username = ?",
                Long.class,
                "profile_user"
        );

        mockMvc.perform(get("/users/detail")
                        .header("Authorization", "Bearer " + session.token())
                        .param("id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("profile_user"))
                .andExpect(jsonPath("$.data.nickname").value("profile_user"));

        mockMvc.perform(post("/users/update")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "email": "profile_updated@example.com",
                                  "nickname": "内容运营"
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile_updated@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("内容运营"));

        mockMvc.perform(get("/users/detail")
                        .header("Authorization", "Bearer " + session.token())
                        .param("id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile_updated@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("内容运营"));
    }

    @Test
    void shouldDisableAndEnableUserWithinTenant() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/users/create")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "operator01",
                                  "password": "admin123",
                                  "email": "operator01@example.com"
                                }
                                """))
                .andExpect(status().isOk());

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM user WHERE username = ?",
                Long.class,
                "operator01"
        );

        mockMvc.perform(post("/users/disable")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "operator01",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("账号已被禁用"));

        mockMvc.perform(post("/users/enable")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "operator01",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("operator01"));
    }

    @Test
    void shouldRejectDisablingCurrentUser() throws Exception {
        LoginSession session = loginDefaultAdmin();

        mockMvc.perform(post("/users/disable")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(session.userId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不能禁用当前登录账号"));
    }

    @Test
    void shouldInvalidateExistingTokenAfterUserDisabled() throws Exception {
        LoginSession owner = ensureAndLogin("suspend_owner", "admin123", "Suspend Owner Tenant");

        mockMvc.perform(post("/users/create")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "suspend_member",
                                  "password": "admin123",
                                  "email": "suspend_member@example.com"
                                }
                                """))
                .andExpect(status().isOk());

        LoginSession member = login("suspend_member", "admin123");
        Long memberId = jdbcTemplate.queryForObject(
                "SELECT id FROM user WHERE username = ?",
                Long.class,
                "suspend_member"
        );

        mockMvc.perform(post("/users/disable")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(memberId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/list")
                        .header("Authorization", "Bearer " + member.token()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("账号已被禁用"));
    }

    @Test
    void shouldChangePasswordForCurrentUser() throws Exception {
        LoginSession session = ensureAndLogin("password_admin", "admin123", "Password Tenant");

        mockMvc.perform(post("/users/change-password")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "admin123",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "password_admin",
                                  "password": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("password_admin"));
    }
}
