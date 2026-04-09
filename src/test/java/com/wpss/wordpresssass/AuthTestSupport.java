package com.wpss.wordpresssass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AuthTestSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected abstract MockMvc mockMvc();

    protected LoginSession loginDefaultAdmin() throws Exception {
        return login("admin", "admin123");
    }

    protected LoginSession ensureAndLogin(String username, String password, String tenantName) throws Exception {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user WHERE username = ?",
                Integer.class,
                username
        );
        if (count != null && count == 0) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO tenant(name, created_at) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, tenantName);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKeys() == null ? null : (Number) keyHolder.getKeys().get("id");
            if (key == null) {
                throw new IllegalStateException("Failed to create tenant");
            }
            Long tenantId = key.longValue();

            jdbcTemplate.update(
                    "INSERT INTO user(tenant_id, username, password, email, nickname, role, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    tenantId,
                    username,
                    passwordEncoder.encode(password),
                    username + "@example.com",
                    username,
                    "ADMIN",
                    "ACTIVE",
                    Timestamp.valueOf(LocalDateTime.now())
            );
        }
        return login(username, password);
    }

    protected LoginSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc().perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return new LoginSession(
                data.path("token").asText(),
                data.path("userId").asLong(),
                data.path("tenantId").asLong(),
                data.path("username").asText()
        );
    }

    protected record LoginSession(
            String token,
            Long userId,
            Long tenantId,
            String username
    ) {
    }
}
