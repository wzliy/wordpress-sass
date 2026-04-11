package com.wpss.wordpresssass.order;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubsiteOrderControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldListOnlyCurrentSiteOrdersAndSupportFilters() throws Exception {
        LoginSession session = ensureAndLogin("subsite_order_owner", "admin123", "Subsite Order Tenant");
        Long siteA = createSite(session, "Orders North", "https://orders-north.example");
        Long siteB = createSite(session, "Orders South", "https://orders-south.example");
        Long categoryId = createCategory(session, "Subsite Bags", "subsite-bags");
        Long productId = createProduct(session, categoryId, "SKU-SUB-ORDER-001", "Subsite Backpack");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteA + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteB + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        String orderNoA = createOrder("orders-north.example", productId, "alice@example.com");
        String orderNoB = createOrder("orders-south.example", productId, "bob@example.com");
        payOrder("orders-north.example", orderNoA);

        mockMvc.perform(get("/api/subsite/orders")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value(orderNoA))
                .andExpect(jsonPath("$.data[0].paymentStatus").value("PAID"));

        mockMvc.perform(get("/api/subsite/orders")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteA))
                        .param("paymentStatus", "PAID")
                        .param("createdFrom", LocalDate.now().toString())
                        .param("createdTo", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value(orderNoA));

        mockMvc.perform(get("/api/subsite/orders")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteB))
                        .param("orderNo", orderNoB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value(orderNoB))
                .andExpect(jsonPath("$.data[0].paymentStatus").value("UNPAID"));
    }

    @Test
    void shouldRejectQueryingAnotherTenantSiteOrders() throws Exception {
        LoginSession owner = ensureAndLogin("subsite_order_owner_2", "admin123", "Subsite Order Owner Tenant");
        LoginSession stranger = ensureAndLogin("subsite_order_stranger", "admin123", "Subsite Order Stranger Tenant");
        Long siteId = createSite(owner, "Private Orders", "https://private-orders.example");

        mockMvc.perform(get("/api/subsite/orders")
                        .header("Authorization", "Bearer " + stranger.token())
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site not found"));
    }

    private String createOrder(String host, Long productId, String email) throws Exception {
        MockHttpSession browserSession = new MockHttpSession();

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", host)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        MvcResult checkoutResult = mockMvc.perform(post("/checkout")
                        .session(browserSession)
                        .header("Host", host)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Order")
                        .param("lastName", "Buyer")
                        .param("email", email)
                        .param("phone", "+15550004444")
                        .param("country", "US")
                        .param("state", "California")
                        .param("city", "San Francisco")
                        .param("postalCode", "94105")
                        .param("addressLine1", "Mission Street 99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/order/*/success"))
                .andReturn();

        String successPath = checkoutResult.getResponse().getRedirectedUrl();
        return successPath.substring("/order/".length(), successPath.length() - "/success".length());
    }

    private void payOrder(String host, String orderNo) throws Exception {
        MvcResult initiateResult = mockMvc.perform(post("/payments/orders/" + orderNo + "/initiate")
                        .header("Host", host)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("providerCode", "MOCK"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payments/mock/*"))
                .andReturn();

        String paymentPath = initiateResult.getResponse().getRedirectedUrl();
        String paymentNo = paymentPath.substring(paymentPath.lastIndexOf('/') + 1);

        mockMvc.perform(post("/payments/mock/" + paymentNo + "/callback")
                        .header("Host", host))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/order/*/success"));
    }

    private Long createSite(LoginSession session, String name, String baseUrl) throws Exception {
        MvcResult addResult = mockMvc.perform(post("/api/admin/sites")
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

        return objectMapper.readTree(addResult.getResponse().getContentAsString())
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
                                  "price": 79.90,
                                  "status": "ACTIVE"
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
