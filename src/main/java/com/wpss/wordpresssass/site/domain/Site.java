package com.wpss.wordpresssass.site.domain;

import java.time.LocalDateTime;

public class Site {

    private final Long id;
    private final Long tenantId;
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
    private final LocalDateTime createdAt;

    public Site(Long id, Long tenantId, String name, SiteType siteType, String baseUrl, String domain, String adminUrl,
                String authType, String wpUsername, String appPassword, SiteStatus status,
                ProvisionStatus provisionStatus, String statusMessage, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
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
        this.createdAt = createdAt;
    }

    public static Site register(Long tenantId, String name, String baseUrl, String wpUsername, String appPassword) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        return new Site(
                null,
                tenantId,
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
                LocalDateTime.now()
        );
    }

    public static Site createProvisioning(Long tenantId, String name, String domain) {
        String normalizedDomain = normalizeBaseUrl(domain);
        return new Site(
                null,
                tenantId,
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
                LocalDateTime.now()
        );
    }

    public Site withId(Long newId) {
        return new Site(newId, tenantId, name, siteType, baseUrl, domain, adminUrl, authType, wpUsername,
                appPassword, status, provisionStatus, statusMessage, createdAt);
    }

    public Site withConnectionStatus(boolean connected, String message) {
        return new Site(
                id,
                tenantId,
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
                createdAt
        );
    }

    public Site withProvisioningCompleted(String baseUrl, String domain, String adminUrl,
                                          String wpUsername, String appPassword, String message) {
        return new Site(
                id,
                tenantId,
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
                createdAt
        );
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
