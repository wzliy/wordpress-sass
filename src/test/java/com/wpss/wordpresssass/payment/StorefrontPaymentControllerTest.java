package com.wpss.wordpresssass.payment;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StorefrontPaymentControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldCreateMockPaymentRecordAndMarkOrderPaidAfterCallback() throws Exception {
        LoginSession session = ensureAndLogin("payment_owner", "admin123", "Payment Owner Tenant");
        Long siteId = createSite(session, "Payment Demo Site", "https://payment-demo.example");
        Long categoryId = createCategory(session, "Payment Category", "payment-category");
        Long productId = createProduct(session, categoryId, "SKU-PAY-001", "Payment Demo Product");
        MockHttpSession browserSession = new MockHttpSession();

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", "payment-demo.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection());

        MvcResult checkoutResult = mockMvc.perform(post("/checkout")
                        .session(browserSession)
                        .header("Host", "payment-demo.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Alice")
                        .param("lastName", "Buyer")
                        .param("email", "alice@example.com")
                        .param("phone", "+15550001111")
                        .param("country", "US")
                        .param("state", "California")
                        .param("city", "San Francisco")
                        .param("postalCode", "94105")
                        .param("addressLine1", "Howard Street 18"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/order/*/success"))
                .andReturn();

        String successPath = checkoutResult.getResponse().getRedirectedUrl();
        assertThat(successPath).isNotBlank();
        String orderNo = extractMiddle(successPath, "/order/", "/success");

        mockMvc.perform(get(successPath)
                        .header("Host", "payment-demo.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pay With Mock")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("UNPAID")));

        MvcResult initiateResult = mockMvc.perform(post("/payments/orders/" + orderNo + "/initiate")
                        .header("Host", "payment-demo.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("providerCode", "MOCK"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payments/mock/*"))
                .andReturn();

        String paymentPath = initiateResult.getResponse().getRedirectedUrl();
        assertThat(paymentPath).isNotBlank();
        String paymentNo = paymentPath.substring(paymentPath.lastIndexOf('/') + 1);

        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM payment_record WHERE tenant_id = ? AND payment_no = ?",
                Integer.class,
                session.tenantId(),
                paymentNo
        );
        String pendingStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM payment_record WHERE tenant_id = ? AND payment_no = ?",
                String.class,
                session.tenantId(),
                paymentNo
        );

        assertThat(paymentCount).isEqualTo(1);
        assertThat(pendingStatus).isEqualTo("PENDING");

        mockMvc.perform(get(paymentPath)
                        .header("Host", "payment-demo.example"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mock Payment")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(paymentNo)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(orderNo)));

        mockMvc.perform(post("/payments/mock/" + paymentNo + "/callback")
                        .header("Host", "payment-demo.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/order/*/success"));

        mockMvc.perform(get("/order/" + orderNo + "/success")
                        .header("Host", "payment-demo.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PAID")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Payment completed")));

        String orderPaymentStatus = jdbcTemplate.queryForObject(
                "SELECT payment_status FROM orders WHERE tenant_id = ? AND order_no = ?",
                String.class,
                session.tenantId(),
                orderNo
        );
        String orderStatus = jdbcTemplate.queryForObject(
                "SELECT order_status FROM orders WHERE tenant_id = ? AND order_no = ?",
                String.class,
                session.tenantId(),
                orderNo
        );
        String paymentStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM payment_record WHERE tenant_id = ? AND payment_no = ?",
                String.class,
                session.tenantId(),
                paymentNo
        );
        String callbackPayload = jdbcTemplate.queryForObject(
                "SELECT callback_payload FROM payment_record WHERE tenant_id = ? AND payment_no = ?",
                String.class,
                session.tenantId(),
                paymentNo
        );

        assertThat(orderPaymentStatus).isEqualTo("PAID");
        assertThat(orderStatus).isEqualTo("PAID");
        assertThat(paymentStatus).isEqualTo("SUCCEEDED");
        assertThat(callbackPayload).contains(orderNo);
    }

    private String extractMiddle(String rawValue, String prefix, String suffix) {
        int start = rawValue.indexOf(prefix);
        int end = rawValue.lastIndexOf(suffix);
        return rawValue.substring(start + prefix.length(), end);
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
                                  "price": 59.90,
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
