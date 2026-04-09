package com.wpss.wordpresssass.site.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wordpress.multisite")
public class MultisiteProperties {

    private boolean enabled;
    private String baseUrl;
    private String provisionEndpoint = "/wp-json/wpss/v1/sites";
    private String adminUsername;
    private String adminAppPassword;
    private String networkDomain = "wp.local";
    private boolean useHttps = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getProvisionEndpoint() {
        return provisionEndpoint;
    }

    public void setProvisionEndpoint(String provisionEndpoint) {
        this.provisionEndpoint = provisionEndpoint;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminAppPassword() {
        return adminAppPassword;
    }

    public void setAdminAppPassword(String adminAppPassword) {
        this.adminAppPassword = adminAppPassword;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public boolean isUseHttps() {
        return useHttps;
    }

    public void setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
    }
}
