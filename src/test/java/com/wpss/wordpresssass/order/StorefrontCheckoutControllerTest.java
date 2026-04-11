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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StorefrontCheckoutControllerTest extends AuthTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Override
    protected MockMvc mockMvc() {
        return mockMvc;
    }

    @Test
    void shouldCreateOrderAndRenderSuccessPage() throws Exception {
        LoginSession session = ensureAndLogin("checkout_owner", "admin123", "Checkout Owner Tenant");
        Long siteId = createSite(session, "Checkout Site", "https://checkout-order.example");
        Long categoryId = createCategory(session, "Checkout Bags", "checkout-bags");
        Long productId = createProduct(session, categoryId, "SKU-ORDER-001", "Checkout Backpack");
        MockHttpSession browserSession = new MockHttpSession();

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", "checkout-order.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection());

        MvcResult checkoutResult = mockMvc.perform(post("/checkout")
                        .session(browserSession)
                        .header("Host", "checkout-order.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "John")
                        .param("lastName", "Smith")
                        .param("email", "john@example.com")
                        .param("phone", "+15550000000")
                        .param("country", "US")
                        .param("state", "California")
                        .param("city", "San Francisco")
                        .param("postalCode", "94105")
                        .param("addressLine1", "Market Street 123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/order/*/success"))
                .andReturn();

        String redirectUrl = checkoutResult.getResponse().getRedirectedUrl();
        assertThat(redirectUrl).isNotBlank();

        mockMvc.perform(get(redirectUrl)
                        .header("Host", "checkout-order.example"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Order Confirmed")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("john@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Checkout Backpack")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$119.70 USD")));

        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM orders WHERE tenant_id = ?", Integer.class, session.tenantId());
        Integer orderItemCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM order_item WHERE tenant_id = ?", Integer.class, session.tenantId());
        Integer emailRecordCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM email_record WHERE tenant_id = ?", Integer.class, session.tenantId());
        String orderNo = jdbcTemplate.queryForObject("SELECT order_no FROM orders WHERE tenant_id = ? LIMIT 1", String.class, session.tenantId());
        String emailTemplateCode = jdbcTemplate.queryForObject("SELECT template_code FROM email_record WHERE tenant_id = ? LIMIT 1", String.class, session.tenantId());
        String emailRecipient = jdbcTemplate.queryForObject("SELECT recipient FROM email_record WHERE tenant_id = ? LIMIT 1", String.class, session.tenantId());
        String emailStatus = jdbcTemplate.queryForObject("SELECT status FROM email_record WHERE tenant_id = ? LIMIT 1", String.class, session.tenantId());
        String emailResponseMessage = jdbcTemplate.queryForObject("SELECT response_message FROM email_record WHERE tenant_id = ? LIMIT 1", String.class, session.tenantId());
        Integer cartSessionItems = browserSession.getAttribute("storefront.cart.by.site") == null ? 0 : 1;

        assertThat(orderCount).isEqualTo(1);
        assertThat(orderItemCount).isEqualTo(1);
        assertThat(emailRecordCount).isEqualTo(1);
        assertThat(orderNo).isNotBlank();
        assertThat(emailTemplateCode).isEqualTo("ORDER_PLACED");
        assertThat(emailRecipient).isEqualTo("john@example.com");
        assertThat(emailStatus).isEqualTo("SENT");
        assertThat(emailResponseMessage).contains(orderNo);
        assertThat(cartSessionItems).isEqualTo(0);

        mockMvc.perform(get("/cart")
                        .session(browserSession)
                        .header("Host", "checkout-order.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your cart is empty.")));
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
                                  "price": 39.90,
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
