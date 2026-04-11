package com.wpss.wordpresssass.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.bootstrap.demo-catalog-enabled=true",
        "spring.datasource.url=jdbc:h2:mem:catalog_demo_bootstrap;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1"
})
class CatalogDemoBootstrapServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSeedDemoCategoriesAndProductsForBootstrapTenant() {
        Integer categoryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM category WHERE tenant_id = (SELECT id FROM tenant WHERE name = 'Default Tenant' LIMIT 1)",
                Integer.class
        );
        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM product WHERE tenant_id = (SELECT id FROM tenant WHERE name = 'Default Tenant' LIMIT 1)",
                Integer.class
        );
        Integer activeProductCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM product WHERE tenant_id = (SELECT id FROM tenant WHERE name = 'Default Tenant' LIMIT 1) AND status = 'ACTIVE'",
                Integer.class
        );
        Integer specificSkuCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM product WHERE tenant_id = (SELECT id FROM tenant WHERE name = 'Default Tenant' LIMIT 1) AND sku IN ('DEMO-OUT-001', 'DEMO-BEA-001', 'DEMO-FIT-001')",
                Integer.class
        );

        assertThat(categoryCount).isEqualTo(3);
        assertThat(productCount).isEqualTo(6);
        assertThat(activeProductCount).isEqualTo(6);
        assertThat(specificSkuCount).isEqualTo(3);
    }
}
