package com.wpss.wordpresssass.shipping;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SupplyShipmentControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldQueryAndUpdateShipmentRecord() throws Exception {
        LoginSession session = ensureAndLogin("supply_owner", "admin123", "Supply Tenant");
        Long siteId = createSite(session, "Supply Site", "https://supply.example");
        Long categoryId = createCategory(session, "Supply Bags", "supply-bags");
        Long productId = createProduct(session, categoryId, "SKU-SUPPLY-001", "Supply Backpack");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        String orderNo = createOrder("supply.example", productId, "supply@example.com");

        mockMvc.perform(get("/api/supply/shipments")
                        .header("Authorization", "Bearer " + session.token())
                        .param("orderNo", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value(orderNo))
                .andExpect(jsonPath("$.data[0].customerEmail").value("supply@example.com"))
                .andExpect(jsonPath("$.data[0].procurementStatus").value("NOT_ORDERED"))
                .andExpect(jsonPath("$.data[0].shipmentStatus").value("NOT_SHIPPED"));

        mockMvc.perform(put("/api/supply/shipments/" + orderNo)
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "procurementStatus": "ORDERED",
                                  "shipmentStatus": "SHIPPED",
                                  "trackingNo": "TRACK-001",
                                  "carrier": "UPS",
                                  "failureReason": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value(orderNo))
                .andExpect(jsonPath("$.data.procurementStatus").value("ORDERED"))
                .andExpect(jsonPath("$.data.shipmentStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.data.trackingNo").value("TRACK-001"))
                .andExpect(jsonPath("$.data.carrier").value("UPS"));

        mockMvc.perform(get("/api/supply/shipments")
                        .header("Authorization", "Bearer " + session.token())
                        .param("trackingNo", "TRACK-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value(orderNo));

        mockMvc.perform(get("/api/supply/shipments")
                        .header("Authorization", "Bearer " + session.token())
                        .param("customerEmail", "supply@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value(orderNo));

        String orderShippingStatus = jdbcTemplate.queryForObject(
                "SELECT shipping_status FROM orders WHERE tenant_id = ? AND order_no = ?",
                String.class,
                session.tenantId(),
                orderNo
        );
        String procurementStatus = jdbcTemplate.queryForObject(
                "SELECT procurement_status FROM shipment_record WHERE tenant_id = ? AND order_id = (SELECT id FROM orders WHERE tenant_id = ? AND order_no = ?)",
                String.class,
                session.tenantId(),
                session.tenantId(),
                orderNo
        );

        assertThat(orderShippingStatus).isEqualTo("SHIPPED");
        assertThat(procurementStatus).isEqualTo("ORDERED");
    }

    @Test
    void shouldRejectUpdatingAnotherTenantShipment() throws Exception {
        LoginSession owner = ensureAndLogin("supply_owner_2", "admin123", "Supply Owner Tenant");
        LoginSession stranger = ensureAndLogin("supply_stranger", "admin123", "Supply Stranger Tenant");
        Long siteId = createSite(owner, "Private Supply Site", "https://private-supply.example");
        Long categoryId = createCategory(owner, "Private Supply Bags", "private-supply-bags");
        Long productId = createProduct(owner, categoryId, "SKU-SUPPLY-002", "Private Supply Backpack");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk());

        String orderNo = createOrder("private-supply.example", productId, "private-supply@example.com");

        mockMvc.perform(put("/api/supply/shipments/" + orderNo)
                        .header("Authorization", "Bearer " + stranger.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "procurementStatus": "ORDERED",
                                  "shipmentStatus": "SHIPPED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));
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
                        .param("firstName", "Supply")
                        .param("lastName", "Buyer")
                        .param("email", email)
                        .param("phone", "+15550005555")
                        .param("country", "US")
                        .param("state", "California")
                        .param("city", "San Francisco")
                        .param("postalCode", "94105")
                        .param("addressLine1", "Howard Street 33"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/order/*/success"))
                .andReturn();

        String successPath = checkoutResult.getResponse().getRedirectedUrl();
        return successPath.substring("/order/".length(), successPath.length() - "/success".length());
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
