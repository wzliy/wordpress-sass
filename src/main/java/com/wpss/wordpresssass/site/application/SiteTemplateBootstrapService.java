package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.domain.SiteTemplateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiteTemplateBootstrapService implements ApplicationRunner {

    private final SiteTemplateRepository siteTemplateRepository;

    public SiteTemplateBootstrapService(SiteTemplateRepository siteTemplateRepository) {
        this.siteTemplateRepository = siteTemplateRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (siteTemplateRepository.countAll() > 0) {
            return;
        }

        List<SiteTemplate> templates = List.of(
                SiteTemplate.builtIn(
                        "starter-one-product",
                        "Starter One Product",
                        "单品转化",
                        "LANDING",
                        null,
                        "聚焦单品转化，预置首屏卖点、倒计时和 FAQ 区块。"
                ),
                SiteTemplate.builtIn(
                        "beauty-flash-sale",
                        "Beauty Flash Sale",
                        "美妆闪促",
                        "SHOP",
                        null,
                        "适合短周期促销活动，内置优惠横幅、评价区和加价购模块。"
                ),
                SiteTemplate.builtIn(
                        "brand-showcase-global",
                        "Brand Showcase Global",
                        "品牌展示",
                        "SHOWCASE",
                        null,
                        "适合品牌官网和多国家投放，预置多区块落地页和品牌故事模块。"
                )
        );

        templates.forEach(siteTemplateRepository::save);
    }
}
