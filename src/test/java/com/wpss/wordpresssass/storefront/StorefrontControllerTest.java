package com.wpss.wordpresssass.storefront;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StorefrontControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldResolveBoundHostToStorefrontPlaceholder() throws Exception {
        LoginSession session = ensureAndLogin("storefront_admin", "admin123", "Storefront Tenant");

        mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Storefront Site",
                                  "baseUrl": "https://storefront.example",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/")
                        .header("Host", "storefront.example"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Storefront Home")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Storefront Site")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No featured products yet")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Track Order")));
    }

    @Test
    void shouldRenderDifferentHomepageContentForDifferentHosts() throws Exception {
        LoginSession session = ensureAndLogin("storefront_multi", "admin123", "Storefront Multi Tenant");

        mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "North Store",
                                  "baseUrl": "https://north.example",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "South Store",
                                  "baseUrl": "https://south.example",
                                  "wpUsername": "admin",
                                  "appPassword": "secret"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/")
                        .header("Host", "north.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("North Store")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("north.example")));

        mockMvc.perform(get("/")
                        .header("Host", "south.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("South Store")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("south.example")));
    }

    @Test
    void shouldReturnFallbackForUnknownHost() throws Exception {
        mockMvc.perform(get("/")
                        .header("Host", "unknown.example"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Site not found")));
    }

    @Test
    void shouldReturnFallbackForDisabledSite() throws Exception {
        LoginSession session = ensureAndLogin("storefront_disabled", "admin123", "Storefront Disabled Tenant");

        MvcResult addResult = mockMvc.perform(post("/site/add")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Disabled Storefront Site",
                                  "baseUrl": "https://disabled-storefront.example",
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
                .andExpect(status().isOk());

        mockMvc.perform(get("/")
                        .header("Host", "disabled-storefront.example"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Site not found")));
    }
}
