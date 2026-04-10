package com.wpss.wordpresssass.site.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Site {

    private final Long id;
    private final Long tenantId;
    private final String siteCode;
    private final String name;
    private final SiteType siteType;
    private final String baseUrl;
    private final String domain;
    private final String adminUrl;
    private final String authType;
    private final String wpUsername;
    private final String appPassword;
    private final SiteStatus status;
    private final ProvisionStatus provisionStatus;
    private final String statusMessage;
    private final Long templateId;
    private final String countryCode;
    private final String languageCode;
    private final String currencyCode;
    private final String themeColor;
    private final String logoUrl;
    private final String bannerTitle;
    private final String bannerSubtitle;
    private final LocalDateTime createdAt;

    public Site(Long id, Long tenantId, String siteCode, String name, SiteType siteType, String baseUrl, String domain, String adminUrl,
                String authType, String wpUsername, String appPassword, SiteStatus status,
                ProvisionStatus provisionStatus, String statusMessage, Long templateId,
                String countryCode, String languageCode, String currencyCode,
                String themeColor, String logoUrl, String bannerTitle, String bannerSubtitle,
                LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteCode = siteCode;
        this.name = name;
        this.siteType = siteType;
        this.baseUrl = baseUrl;
        this.domain = domain;
        this.adminUrl = adminUrl;
        this.authType = authType;
        this.wpUsername = wpUsername;
        this.appPassword = appPassword;
        this.status = status;
        this.provisionStatus = provisionStatus;
        this.statusMessage = statusMessage;
        this.templateId = templateId;
        this.countryCode = countryCode;
        this.languageCode = languageCode;
        this.currencyCode = currencyCode;
        this.themeColor = themeColor;
        this.logoUrl = logoUrl;
        this.bannerTitle = bannerTitle;
        this.bannerSubtitle = bannerSubtitle;
        this.createdAt = createdAt;
    }

    public static Site register(Long tenantId, String name, String baseUrl, String wpUsername, String appPassword) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        return new Site(
                null,
                tenantId,
                generateSiteCode(name),
                name,
                SiteType.REGISTERED,
                normalizedBaseUrl,
                normalizedBaseUrl,
                normalizedBaseUrl + "/wp-admin",
                "APP_PASSWORD",
                wpUsername,
                appPassword,
                SiteStatus.ENABLED,
                ProvisionStatus.NONE,
                "CREATED",
                null,
                null,
                null,
                null,
                "#2563EB",
                null,
                name,
                "Your storefront is ready to be customized.",
                LocalDateTime.now()
        );
    }

    public static Site createProvisioning(Long tenantId,
                                          String name,
                                          String domain,
                                          Long templateId,
                                          String countryCode,
                                          String languageCode,
                                          String currencyCode) {
        String normalizedDomain = normalizeBaseUrl(domain);
        return new Site(
                null,
                tenantId,
                generateSiteCode(name),
                name,
                SiteType.PROVISIONED,
                normalizedDomain,
                normalizedDomain,
                normalizedDomain + "/wp-admin",
                "APP_PASSWORD",
                "pending",
                "pending",
                SiteStatus.DISABLED,
                ProvisionStatus.PROVISIONING,
                "Provisioning site",
                templateId,
                countryCode,
                languageCode,
                currencyCode,
                "#2563EB",
                null,
                name,
                "Provisioning storefront assets and default homepage blocks.",
                LocalDateTime.now()
        );
    }

    public Site withId(Long newId) {
        return new Site(newId, tenantId, siteCode, name, siteType, baseUrl, domain, adminUrl, authType, wpUsername,
                appPassword, status, provisionStatus, statusMessage, templateId, countryCode, languageCode, currencyCode,
                themeColor, logoUrl, bannerTitle, bannerSubtitle, createdAt);
    }

    public Site withConnectionStatus(boolean connected, String message) {
        return new Site(
                id,
                tenantId,
                siteCode,
                name,
                siteType,
                baseUrl,
                domain,
                adminUrl,
                authType,
                wpUsername,
                appPassword,
                connected ? SiteStatus.ENABLED : SiteStatus.DISABLED,
                provisionStatus,
                message,
                templateId,
                countryCode,
                languageCode,
                currencyCode,
                themeColor,
                logoUrl,
                bannerTitle,
                bannerSubtitle,
                createdAt
        );
    }

    public Site withProvisioningCompleted(String baseUrl, String domain, String adminUrl,
                                          String wpUsername, String appPassword, String message) {
        return new Site(
                id,
                tenantId,
                siteCode,
                name,
                SiteType.PROVISIONED,
                normalizeBaseUrl(baseUrl),
                normalizeBaseUrl(domain),
                normalizeBaseUrl(adminUrl),
                authType,
                wpUsername,
                appPassword,
                SiteStatus.ENABLED,
                ProvisionStatus.ACTIVE,
                message,
                templateId,
                countryCode,
                languageCode,
                currencyCode,
                themeColor,
                logoUrl,
                bannerTitle,
                bannerSubtitle,
                createdAt
        );
    }

    private static String generateSiteCode(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        String prefix = normalized.isBlank() ? "site" : normalized;
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + "-" + suffix;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getName() {
        return name;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getDomain() {
        return domain;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public String getWpUsername() {
        return wpUsername;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public ProvisionStatus getProvisionStatus() {
        return provisionStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getBannerTitle() {
        return bannerTitle;
    }

    public String getBannerSubtitle() {
        return bannerSubtitle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
