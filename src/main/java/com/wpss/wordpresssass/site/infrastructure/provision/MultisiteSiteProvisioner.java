package com.wpss.wordpresssass.site.infrastructure.provision;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.config.MultisiteProperties;
import com.wpss.wordpresssass.site.domain.Site;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@ConditionalOnProperty(prefix = "wordpress.multisite", name = "enabled", havingValue = "true")
public class MultisiteSiteProvisioner implements SiteProvisioner {

    private final RestClient restClient;
    private final MultisiteProperties multisiteProperties;

    public MultisiteSiteProvisioner(RestClient.Builder restClientBuilder, MultisiteProperties multisiteProperties) {
        this.restClient = restClientBuilder.build();
        this.multisiteProperties = multisiteProperties;
    }

    @Override
    public ProvisionedSite provision(Site site, ProvisionContext context) {
        validateProperties();
        String endpoint = normalizeBaseUrl(multisiteProperties.getBaseUrl()) + multisiteProperties.getProvisionEndpoint();
        String auth = multisiteProperties.getAdminUsername() + ":" + multisiteProperties.getAdminAppPassword();
        String basicToken = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        try {
            MultisiteProvisionResponse response = restClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new MultisiteProvisionRequest(
                            site.getName(),
                            context.subdomainPrefix(),
                            context.adminEmail(),
                            site.getTenantId()
                    ))
                    .retrieve()
                    .body(MultisiteProvisionResponse.class);

            if (response == null) {
                throw new BusinessException("Multisite provision response is empty");
            }

            return new ProvisionedSite(
                    response.baseUrl(),
                    response.domain(),
                    response.adminUrl(),
                    response.wpUsername(),
                    response.appPassword(),
                    response.message() == null ? "Multisite provision completed" : response.message()
            );
        } catch (RestClientException ex) {
            throw new BusinessException("Multisite provision failed: " + ex.getMessage());
        }
    }

    private void validateProperties() {
        if (isBlank(multisiteProperties.getBaseUrl())
                || isBlank(multisiteProperties.getAdminUsername())
                || isBlank(multisiteProperties.getAdminAppPassword())) {
            throw new BusinessException("WordPress multisite configuration is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeBaseUrl(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
