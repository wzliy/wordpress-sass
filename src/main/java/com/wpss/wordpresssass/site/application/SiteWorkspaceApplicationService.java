package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageRepository;
import com.wpss.wordpresssass.site.application.dto.SiteWorkspaceDto;
import com.wpss.wordpresssass.site.domain.ProvisionStatus;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteSettingRepository;
import com.wpss.wordpresssass.site.domain.SiteStatus;
import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.domain.SiteTemplateRepository;
import com.wpss.wordpresssass.site.domain.ThemeConfigRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SiteWorkspaceApplicationService {

    private final SiteRepository siteRepository;
    private final SiteTemplateRepository siteTemplateRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final ThemeConfigRepository themeConfigRepository;
    private final PageRepository pageRepository;

    public SiteWorkspaceApplicationService(SiteRepository siteRepository,
                                           SiteTemplateRepository siteTemplateRepository,
                                           SiteSettingRepository siteSettingRepository,
                                           ThemeConfigRepository themeConfigRepository,
                                           PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.siteTemplateRepository = siteTemplateRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.themeConfigRepository = themeConfigRepository;
        this.pageRepository = pageRepository;
    }

    public SiteWorkspaceDto getWorkspace(Long siteId) {
        Long tenantId = requireTenantId();
        Site site = siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
        SiteTemplate siteTemplate = site.getTemplateId() == null
                ? null
                : siteTemplateRepository.findAccessibleById(tenantId, site.getTemplateId()).orElse(null);
        boolean initialized = siteSettingRepository.existsBySite(site.getTenantId(), site.getId())
                && themeConfigRepository.existsBySite(site.getTenantId(), site.getId());

        List<SiteWorkspaceDto.ReadinessItem> readinessItems = buildReadinessItems(site, siteTemplate, initialized);
        SiteWorkspaceDto.Readiness readiness = new SiteWorkspaceDto.Readiness(
                calculateReadinessScore(readinessItems),
                resolveReadinessLevel(site, readinessItems),
                readinessItems
        );
        List<SiteWorkspaceDto.Alert> alerts = buildAlerts(site);
        return new SiteWorkspaceDto(
                site.getId(),
                site.getTenantId(),
                resolveWorkspaceStatus(site, readinessItems, alerts),
                LocalDateTime.now(),
                buildProfile(site, siteTemplate),
                readiness,
                buildModuleSummaries(site),
                buildPendingTasks(site),
                alerts,
                buildRecentActivities(site),
                buildQuickActions(site)
        );
    }

    private SiteWorkspaceDto.Profile buildProfile(Site site, SiteTemplate siteTemplate) {
        return new SiteWorkspaceDto.Profile(
                site.getId(),
                site.getName(),
                site.getSiteType().name(),
                site.getDomain(),
                site.getBaseUrl(),
                site.getAdminUrl(),
                toSiteStatus(site),
                site.getProvisionStatus().name(),
                site.getStatusMessage(),
                siteTemplate == null ? null : siteTemplate.getCode(),
                siteTemplate == null ? null : siteTemplate.getName(),
                site.getCountryCode(),
                site.getLanguageCode(),
                site.getCurrencyCode(),
                site.getCreatedAt(),
                null
        );
    }

    private List<SiteWorkspaceDto.ReadinessItem> buildReadinessItems(Site site,
                                                                     SiteTemplate siteTemplate,
                                                                     boolean initialized) {
        List<SiteWorkspaceDto.ReadinessItem> items = new ArrayList<>();
        boolean siteEnabled = site.getStatus() == SiteStatus.ENABLED;
        boolean hasDomain = site.getDomain() != null && !site.getDomain().isBlank();

        items.add(new SiteWorkspaceDto.ReadinessItem(
                "SITE_ACCESSIBLE",
                "站点可访问",
                siteEnabled ? "DONE" : "WARNING",
                siteEnabled ? "站点当前可访问，可继续进入运营配置" : fallback(site.getStatusMessage(), "站点连接状态异常，请先检查连接"),
                "OPEN_SITE"
        ));
        items.add(new SiteWorkspaceDto.ReadinessItem(
                "TEMPLATE_BOUND",
                "模板已绑定",
                siteTemplate == null ? "TODO" : "DONE",
                siteTemplate == null ? "尚未选择建站模板" : "当前模板为 " + siteTemplate.getName(),
                "GO_TEMPLATE_CENTER"
        ));
        items.add(new SiteWorkspaceDto.ReadinessItem(
                "PAGE_STRUCTURE_READY",
                "页面骨架已初始化",
                site.getProvisionStatus() == ProvisionStatus.PROVISIONING ? "BLOCKED" : initialized ? "DONE" : "TODO",
                site.getProvisionStatus() == ProvisionStatus.PROVISIONING
                        ? "站点仍在初始化，页面骨架稍后再生成"
                        : initialized ? "默认页面骨架、主题变量和基础配置已生成" : "默认页面骨架和主题配置尚未生成",
                "GO_LAYOUT_EDITOR"
        ));
        items.add(new SiteWorkspaceDto.ReadinessItem(
                "PAYMENT_BOUND",
                "支付已绑定",
                "TODO",
                "尚未配置默认支付通道",
                "GO_PAYMENT_BIND"
        ));
        items.add(new SiteWorkspaceDto.ReadinessItem(
                "CLOAK_CONFIGURED",
                "斗篷规则已配置",
                "TODO",
                "尚未配置斗篷规则",
                "GO_CLOAK"
        ));
        items.add(new SiteWorkspaceDto.ReadinessItem(
                "DOMAIN_SSL_READY",
                "域名与 SSL 状态",
                hasDomain ? "WARNING" : "TODO",
                hasDomain ? "访问域名已分配，SSL 状态将在域名中心接入" : "站点还没有可用域名",
                "GO_DOMAIN_CENTER"
        ));
        return items;
    }

    private int calculateReadinessScore(List<SiteWorkspaceDto.ReadinessItem> items) {
        int score = 0;
        for (SiteWorkspaceDto.ReadinessItem item : items) {
            if ("DONE".equals(item.status())) {
                score += 20;
            } else if ("WARNING".equals(item.status())) {
                score += 10;
            }
        }
        return Math.min(score, 100);
    }

    private String resolveReadinessLevel(Site site, List<SiteWorkspaceDto.ReadinessItem> items) {
        if (site.getProvisionStatus() == ProvisionStatus.FAILED || site.getStatus() == SiteStatus.DISABLED) {
            return "RISK";
        }
        int score = calculateReadinessScore(items);
        if (score >= 80) {
            return "READY";
        }
        if (score >= 40) {
            return "BASIC_READY";
        }
        return "NOT_READY";
    }

    private String resolveWorkspaceStatus(Site site,
                                          List<SiteWorkspaceDto.ReadinessItem> readinessItems,
                                          List<SiteWorkspaceDto.Alert> alerts) {
        if (site.getProvisionStatus() == ProvisionStatus.PROVISIONING) {
            return "CREATING";
        }
        if (site.getProvisionStatus() == ProvisionStatus.FAILED) {
            return "AT_RISK";
        }
        if (alerts.stream().anyMatch(alert -> "HIGH".equals(alert.level()) || "CRITICAL".equals(alert.level()))) {
            return "AT_RISK";
        }
        if (site.getStatus() == SiteStatus.DISABLED) {
            return "DISABLED";
        }
        boolean actionRequired = readinessItems.stream()
                .anyMatch(item -> !"DONE".equals(item.status()));
        return actionRequired ? "ACTION_REQUIRED" : "ACTIVE";
    }

    private List<SiteWorkspaceDto.ModuleSummary> buildModuleSummaries(Site site) {
        boolean enabled = site.getStatus() == SiteStatus.ENABLED;
        boolean provisioning = site.getProvisionStatus() == ProvisionStatus.PROVISIONING;
        List<Page> pages = pageRepository.findBySite(site.getTenantId(), site.getId());
        boolean hasHomePage = pages.stream().anyMatch(page -> Page.HOME_PAGE_KEY.equals(page.getPageKey()));
        long publishedPageCount = pages.stream().filter(page -> page.getPublishedVersionId() != null).count();
        String editorPath = hasHomePage ? "/sites/" + site.getId() + "/pages/home/editor" : null;

        return List.of(
                new SiteWorkspaceDto.ModuleSummary(
                        "PAGE",
                        provisioning ? "CONFIGURING" : hasHomePage ? "READY" : "ACTION_REQUIRED",
                        "页面概览",
                        new SiteWorkspaceDto.Metric("页面状态", provisioning ? "初始化中" : hasHomePage ? "首页已接入" : "待接入", provisioning ? "warning" : hasHomePage ? "success" : "muted"),
                        List.of(
                                new SiteWorkspaceDto.Metric("已发布页面", String.valueOf(publishedPageCount), publishedPageCount > 0 ? "success" : "neutral"),
                                new SiteWorkspaceDto.Metric("编辑入口", hasHomePage ? "已开放" : "待开放", hasHomePage ? "success" : "neutral")
                        ),
                        hasHomePage
                                ? List.of("系统页面已初始化，可编辑首页、商品详情、结账页和成功页", "可直接进入首页编辑器继续保存草稿、回滚历史版本或发布")
                                : List.of("页面装修引擎尚未接入当前站点", "需先补齐首页页面初始化"),
                        List.of(new SiteWorkspaceDto.Action("GO_LAYOUT_EDITOR", "页面装修", editorPath, "INTERNAL", hasHomePage))
                ),
                new SiteWorkspaceDto.ModuleSummary(
                        "PAYMENT",
                        enabled ? "ACTION_REQUIRED" : "DISABLED",
                        "支付概览",
                        new SiteWorkspaceDto.Metric("默认通道", "未绑定", "danger"),
                        List.of(
                                new SiteWorkspaceDto.Metric("支付成功率", "--", "neutral"),
                                new SiteWorkspaceDto.Metric("异常回调", "0", "neutral")
                        ),
                        List.of("支付主链路未配置", "需在支付中心建立默认通道"),
                        List.of(new SiteWorkspaceDto.Action("GO_PAYMENT_BIND", "绑定支付", null, "INTERNAL", false))
                ),
                new SiteWorkspaceDto.ModuleSummary(
                        "CLOAK",
                        enabled ? "ACTION_REQUIRED" : "DISABLED",
                        "斗篷概览",
                        new SiteWorkspaceDto.Metric("规则状态", "未配置", "warning"),
                        List.of(
                                new SiteWorkspaceDto.Metric("生效规则", "0", "neutral"),
                                new SiteWorkspaceDto.Metric("异常日志", "0", "neutral")
                        ),
                        List.of("斗篷引擎模块待接入", "工作台先暴露配置缺口"),
                        List.of(new SiteWorkspaceDto.Action("GO_CLOAK", "配置斗篷", null, "INTERNAL", false))
                ),
                new SiteWorkspaceDto.ModuleSummary(
                        "ORDER",
                        enabled ? "UNAVAILABLE" : "DISABLED",
                        "订单概览",
                        new SiteWorkspaceDto.Metric("今日订单", "0", "neutral"),
                        List.of(
                                new SiteWorkspaceDto.Metric("待支付", "0", "neutral"),
                                new SiteWorkspaceDto.Metric("异常订单", "0", "neutral")
                        ),
                        List.of("订单中心模型未建立", "首版工作台先保留摘要卡片"),
                        List.of(new SiteWorkspaceDto.Action("GO_ORDER_CENTER", "查看订单", null, "INTERNAL", false))
                ),
                new SiteWorkspaceDto.ModuleSummary(
                        "DOMAIN",
                        site.getDomain() == null || site.getDomain().isBlank() ? "ACTION_REQUIRED" : "READY",
                        "域名概览",
                        new SiteWorkspaceDto.Metric("访问域名", fallback(site.getDomain(), "未分配"), site.getDomain() == null || site.getDomain().isBlank() ? "warning" : "success"),
                        List.of(
                                new SiteWorkspaceDto.Metric("后台地址", fallback(site.getAdminUrl(), "--"), "neutral"),
                                new SiteWorkspaceDto.Metric("SSL 状态", "待接入", "neutral")
                        ),
                        List.of("域名已纳入工作台摘要", "SSL 与 DNS 适配器将在 M16 接入"),
                        List.of(new SiteWorkspaceDto.Action("OPEN_ADMIN", "打开后台", site.getAdminUrl(), "EXTERNAL", site.getAdminUrl() != null && !site.getAdminUrl().isBlank()))
                )
        );
    }

    private List<SiteWorkspaceDto.PendingTask> buildPendingTasks(Site site) {
        if (site.getProvisionStatus() != ProvisionStatus.PROVISIONING) {
            return List.of();
        }
        return List.of(new SiteWorkspaceDto.PendingTask(
                null,
                "SITE_PROVISION",
                "RUNNING",
                "站点建站中",
                fallback(site.getStatusMessage(), "站点仍在初始化，请稍后刷新"),
                null,
                site.getCreatedAt(),
                0
        ));
    }

    private List<SiteWorkspaceDto.Alert> buildAlerts(Site site) {
        List<SiteWorkspaceDto.Alert> alerts = new ArrayList<>();
        if (site.getProvisionStatus() == ProvisionStatus.PROVISIONING) {
            alerts.add(new SiteWorkspaceDto.Alert(
                    "INFO",
                    "SITE_PROVISIONING",
                    "站点仍在初始化",
                    fallback(site.getStatusMessage(), "系统正在创建站点和后台入口"),
                    "REFRESH_WORKSPACE",
                    site.getCreatedAt()
            ));
        }
        if (site.getProvisionStatus() == ProvisionStatus.FAILED) {
            alerts.add(new SiteWorkspaceDto.Alert(
                    "HIGH",
                    "SITE_PROVISION_FAILED",
                    "建站失败",
                    fallback(site.getStatusMessage(), "建站任务执行失败，请检查错误原因"),
                    "RETRY_PROVISION",
                    site.getCreatedAt()
            ));
        }
        if (site.getStatus() == SiteStatus.DISABLED) {
            alerts.add(new SiteWorkspaceDto.Alert(
                    "WARNING",
                    "SITE_CONNECTION_CHECK",
                    "站点当前不可用",
                    fallback(site.getStatusMessage(), "最近一次连接检查未通过"),
                    "OPEN_ADMIN",
                    site.getCreatedAt()
            ));
        }
        return alerts;
    }

    private List<SiteWorkspaceDto.Activity> buildRecentActivities(Site site) {
        List<SiteWorkspaceDto.Activity> activities = new ArrayList<>();
        activities.add(new SiteWorkspaceDto.Activity(
                site.getSiteType().name().equals("PROVISIONED") ? "PROVISION" : "SITE_REGISTER",
                site.getSiteType().name().equals("PROVISIONED") ? "站点已纳入建站主线" : "站点已接入平台",
                site.getSiteType().name().equals("PROVISIONED")
                        ? fallback(site.getStatusMessage(), "站点已创建，可进入工作台继续配置")
                        : "已记录站点地址和后台入口，可继续测试连接",
                "system",
                site.getCreatedAt(),
                site.getId(),
                "SITE"
        ));
        if (site.getStatusMessage() != null && !site.getStatusMessage().isBlank()) {
            activities.add(new SiteWorkspaceDto.Activity(
                    "SITE_STATUS",
                    "最近状态摘要",
                    site.getStatusMessage(),
                    "system",
                    site.getCreatedAt(),
                    site.getId(),
                    "SITE"
            ));
        }
        return activities;
    }

    private List<SiteWorkspaceDto.Action> buildQuickActions(Site site) {
        boolean hasHomePage = pageRepository.findBySiteAndPageKey(site.getTenantId(), site.getId(), Page.HOME_PAGE_KEY).isPresent();
        return List.of(
                new SiteWorkspaceDto.Action("OPEN_SITE", "打开站点", site.getBaseUrl(), "EXTERNAL", site.getBaseUrl() != null && !site.getBaseUrl().isBlank()),
                new SiteWorkspaceDto.Action("OPEN_ADMIN", "打开后台", site.getAdminUrl(), "EXTERNAL", site.getAdminUrl() != null && !site.getAdminUrl().isBlank()),
                new SiteWorkspaceDto.Action("GO_LAYOUT_EDITOR", "首页装修", hasHomePage ? "/sites/" + site.getId() + "/pages/home/editor" : null, "INTERNAL", hasHomePage),
                new SiteWorkspaceDto.Action("BACK_TO_LIST", "返回站点列表", "/sites/list", "INTERNAL", true),
                new SiteWorkspaceDto.Action("GO_PROVISION", "继续建站", "/sites/provision", "INTERNAL", true)
        );
    }

    private String toSiteStatus(Site site) {
        return site.getStatus() == SiteStatus.ENABLED ? "ACTIVE" : "DISABLED";
    }

    private String fallback(String value, String fallbackValue) {
        return value == null || value.isBlank() ? fallbackValue : value;
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
