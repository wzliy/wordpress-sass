package com.wpss.wordpresssass.report;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminReportControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldAggregateOrdersRevenueAndSiteSummary() throws Exception {
        LoginSession session = ensureAndLogin("report_owner", "admin123", "Report Tenant");
        Long siteA = createSite(session, "Report North", "https://report-north.example");
        Long siteB = createSite(session, "Report South", "https://report-south.example");
        Long categoryId = createCategory(session, "Report Bags", "report-bags");
        Long productId = createProduct(session, categoryId, "SKU-REPORT-001", "Report Backpack");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteA + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteB + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        String orderNoA = createOrder("report-north.example", productId, "report-a@example.com");
        String orderNoB = createOrder("report-south.example", productId, "report-b@example.com");

        payOrder("report-north.example", orderNoA);
        shipOrder(session, orderNoA);

        mockMvc.perform(get("/api/admin/reports")
                        .header("Authorization", "Bearer " + session.token())
                        .param("dateFrom", LocalDate.now().toString())
                        .param("dateTo", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOrders").value(2))
                .andExpect(jsonPath("$.data.paidOrders").value(1))
                .andExpect(jsonPath("$.data.shippedOrders").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(79.80))
                .andExpect(jsonPath("$.data.siteSummaries.length()").value(2))
                .andExpect(jsonPath("$.data.siteSummaries[?(@.siteId==" + siteA + ")].paidOrders").value(org.hamcrest.Matchers.hasItem(1)))
                .andExpect(jsonPath("$.data.siteSummaries[?(@.siteId==" + siteB + ")].paidOrders").value(org.hamcrest.Matchers.hasItem(0)));

        mockMvc.perform(get("/api/admin/reports")
                        .header("Authorization", "Bearer " + session.token())
                        .param("siteId", String.valueOf(siteA))
                        .param("dateFrom", LocalDate.now().toString())
                        .param("dateTo", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOrders").value(1))
                .andExpect(jsonPath("$.data.paidOrders").value(1))
                .andExpect(jsonPath("$.data.shippedOrders").value(1))
                .andExpect(jsonPath("$.data.siteSummaries.length()").value(1))
                .andExpect(jsonPath("$.data.siteSummaries[0].siteId").value(siteA))
                .andExpect(jsonPath("$.data.siteSummaries[0].totalRevenue").value(79.80));
    }

    private void shipOrder(LoginSession session, String orderNo) throws Exception {
        mockMvc.perform(put("/api/supply/shipments/" + orderNo)
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "procurementStatus": "ORDERED",
                                  "shipmentStatus": "SHIPPED",
                                  "trackingNo": "REPORT-TRACK-001",
                                  "carrier": "UPS"
                                }
                                """))
                .andExpect(status().isOk());
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
                        .param("firstName", "Report")
                        .param("lastName", "Buyer")
                        .param("email", email)
                        .param("phone", "+15550006666")
                        .param("country", "US")
                        .param("state", "California")
                        .param("city", "San Francisco")
                        .param("postalCode", "94105")
                        .param("addressLine1", "Folsom Street 9"))
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
                                  "price": 69.90,
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
