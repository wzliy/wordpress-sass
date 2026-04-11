package com.wpss.wordpresssass.storefront;

import com.wpss.wordpresssass.AuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    @Test
    void shouldOnlyShowFeaturedProductsWhenPublishedToSite() throws Exception {
        LoginSession session = ensureAndLogin("storefront_featured_publish", "admin123", "Storefront Featured Publish Tenant");
        Long siteId = createSite(session, "Featured Publish Site", "https://featured-publish.example");
        Long categoryId = createCategory(session, "Featured Category", "featured-category");
        Long productId = createProduct(session, categoryId, "SKU-FEATURED-1", "Featured Product");

        jdbcTemplate.update(
                "UPDATE site_homepage_config SET config_json = ?, updated_at = ? WHERE tenant_id = ? AND site_id = ?",
                """
                        {"bannerTitle":"Featured Publish Site","bannerSubtitle":"Filtered by publish status","themeColor":"#2563EB","featuredProductIds":["%d"],"menuItems":[{"label":"Home","path":"/"},{"label":"Catalog","path":"/category/all"},{"label":"Track Order","path":"/track"}]}
                        """.formatted(productId).trim(),
                Timestamp.valueOf(LocalDateTime.now()),
                session.tenantId(),
                siteId
        );

        mockMvc.perform(get("/")
                        .header("Host", "featured-publish.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No featured products yet")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Product #" + productId))));

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/")
                        .header("Host", "featured-publish.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Product #" + productId)));

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/unpublish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/")
                        .header("Host", "featured-publish.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No featured products yet")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Product #" + productId))));
    }

    @Test
    void shouldRenderPublishedProductsOnCategoryPageAndSupportKeywordSearch() throws Exception {
        LoginSession session = ensureAndLogin("storefront_category_search", "admin123", "Storefront Category Search Tenant");
        Long siteId = createSite(session, "Category Search Site", "https://category-search.example");
        Long bagsId = createCategory(session, "Bags", "bags");
        Long shoesId = createCategory(session, "Shoes", "shoes");
        Long backpackId = createProduct(session, bagsId, "SKU-BAG-001", "Travel Backpack");
        Long shoesIdProduct = createProduct(session, shoesId, "SKU-SHOE-001", "Runner Shoes");
        createProduct(session, bagsId, "SKU-BAG-002", "Hidden Duffel");

        mockMvc.perform(post("/api/admin/products/" + backpackId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/products/" + shoesIdProduct + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/category/bags")
                        .header("Host", "category-search.example"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Travel Backpack")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Runner Shoes"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden Duffel"))));

        mockMvc.perform(get("/category/all")
                        .header("Host", "category-search.example")
                        .param("q", "shoe"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Runner Shoes")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Travel Backpack"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden Duffel"))));
    }

    @Test
    void shouldRenderPublishedActiveProductDetail() throws Exception {
        LoginSession session = ensureAndLogin("storefront_product_detail", "admin123", "Storefront Product Detail Tenant");
        Long siteId = createSite(session, "Product Detail Site", "https://product-detail.example");
        Long categoryId = createCategory(session, "Outerwear", "outerwear");
        Long productId = createDetailedProduct(session, categoryId);

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/product/" + productId)
                        .header("Host", "product-detail.example"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Storm Shield Jacket")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SKU-DETAIL-001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Waterproof shell built for cold-weather paid traffic landing pages.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://cdn.example.com/storm-jacket-2.jpg")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("L")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$129.90")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$169.90")));
    }

    @Test
    void shouldHideInactiveProductFromCategoryAndDetailEvenWhenPublished() throws Exception {
        LoginSession session = ensureAndLogin("storefront_inactive_hidden", "admin123", "Storefront Inactive Hidden Tenant");
        Long siteId = createSite(session, "Inactive Hidden Site", "https://inactive-hidden.example");
        Long categoryId = createCategory(session, "Gear", "gear");
        Long productId = createProduct(session, categoryId, "SKU-HIDE-001", "Hidden After Deactivate");

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/product/" + productId)
                        .header("Host", "inactive-hidden.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hidden After Deactivate")));

        mockMvc.perform(post("/api/admin/products/" + productId + "/deactivate")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/category/all")
                        .header("Host", "inactive-hidden.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden After Deactivate"))));

        mockMvc.perform(get("/product/" + productId)
                        .header("Host", "inactive-hidden.example"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Site not found")));
    }

    @Test
    void shouldAddUpdateAndRemoveCartItemsInSession() throws Exception {
        LoginSession session = ensureAndLogin("storefront_cart_session", "admin123", "Storefront Cart Session Tenant");
        Long siteId = createSite(session, "Cart Session Site", "https://cart-session.example");
        Long categoryId = createCategory(session, "Cart Gear", "cart-gear");
        Long productId = createProduct(session, categoryId, "SKU-CART-001", "Cart Backpack");
        MockHttpSession browserSession = new MockHttpSession();

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", "cart-session.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/cart")
                        .session(browserSession)
                        .header("Host", "cart-session.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cart Backpack")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$79.80")));

        mockMvc.perform(post("/cart/items/" + productId + "/quantity")
                        .session(browserSession)
                        .header("Host", "cart-session.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("quantity", "4"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/cart")
                        .session(browserSession)
                        .header("Host", "cart-session.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$159.60")));

        mockMvc.perform(post("/cart/items/" + productId + "/remove")
                        .session(browserSession)
                        .header("Host", "cart-session.example"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/cart")
                        .session(browserSession)
                        .header("Host", "cart-session.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your cart is empty.")));
    }

    @Test
    void shouldKeepCartBucketsIsolatedPerSite() throws Exception {
        LoginSession session = ensureAndLogin("storefront_cart_isolation", "admin123", "Storefront Cart Isolation Tenant");
        Long northSiteId = createSite(session, "North Cart Site", "https://north-cart.example");
        Long southSiteId = createSite(session, "South Cart Site", "https://south-cart.example");
        Long bagsId = createCategory(session, "Bags", "bags");
        Long shoesId = createCategory(session, "Shoes", "shoes");
        Long northProductId = createProduct(session, bagsId, "SKU-NORTH-CART", "North Bag");
        Long southProductId = createProduct(session, shoesId, "SKU-SOUTH-CART", "South Shoe");
        MockHttpSession browserSession = new MockHttpSession();

        mockMvc.perform(post("/api/admin/products/" + northProductId + "/publishes/" + northSiteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/products/" + southProductId + "/publishes/" + southSiteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", "north-cart.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(northProductId))
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", "south-cart.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(southProductId))
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/cart")
                        .session(browserSession)
                        .header("Host", "north-cart.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("North Bag")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("South Shoe"))));

        mockMvc.perform(get("/cart")
                        .session(browserSession)
                        .header("Host", "south-cart.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("South Shoe")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("North Bag"))));
    }

    @Test
    void shouldRenderCheckoutFormFromCartSession() throws Exception {
        LoginSession session = ensureAndLogin("storefront_checkout_form", "admin123", "Storefront Checkout Form Tenant");
        Long siteId = createSite(session, "Checkout Form Site", "https://checkout-form.example");
        Long categoryId = createCategory(session, "Checkout Gear", "checkout-gear");
        Long productId = createProduct(session, categoryId, "SKU-CHECKOUT-001", "Checkout Product");
        MockHttpSession browserSession = new MockHttpSession();

        mockMvc.perform(post("/api/admin/products/" + productId + "/publishes/" + siteId + "/publish")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .session(browserSession)
                        .header("Host", "checkout-form.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/checkout")
                        .session(browserSession)
                        .header("Host", "checkout-form.example"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Checkout")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shipping Details")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Checkout Product")));
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

    private Long createDetailedProduct(LoginSession session, Long categoryId) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-DETAIL-001",
                                  "title": "Storm Shield Jacket",
                                  "categoryId": %d,
                                  "coverImage": "https://cdn.example.com/storm-jacket-cover.jpg",
                                  "galleryImages": [
                                    "https://cdn.example.com/storm-jacket-1.jpg",
                                    "https://cdn.example.com/storm-jacket-2.jpg"
                                  ],
                                  "descriptionHtml": "<p>Waterproof shell built for cold-weather paid traffic landing pages.</p>",
                                  "sizes": ["M", "L", "XL"],
                                  "price": 129.90,
                                  "compareAtPrice": 169.90,
                                  "status": "ACTIVE"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }
}
