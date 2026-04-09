package com.wpss.wordpresssass.site.infrastructure.wordpress;

import com.wpss.wordpresssass.post.domain.Post;
import com.wpss.wordpresssass.site.domain.Site;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class RestWpClient implements WpClient {

    private final RestClient restClient;

    public RestWpClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public WpConnectionResult testConnection(Site site) {
        String auth = site.getWpUsername() + ":" + site.getAppPassword();
        String basicToken = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String endpoint = site.getBaseUrl() + "/wp-json/wp/v2/users/me";

        try {
            restClient.get()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
            return new WpConnectionResult(true, "Connection successful");
        } catch (RestClientException ex) {
            return new WpConnectionResult(false, ex.getMessage());
        }
    }

    @Override
    public WpPublishResult publishPost(Site site, Post post, String targetStatus) {
        String auth = site.getWpUsername() + ":" + site.getAppPassword();
        String basicToken = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String endpoint = site.getBaseUrl() + "/wp-json/wp/v2/posts";
        Map<String, Object> requestBody = Map.of(
                "title", post.getTitle(),
                "content", post.getContent(),
                "status", targetStatus
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = restClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            Long remotePostId = extractLong(responseBody, "id");
            String remotePostUrl = responseBody == null ? null : String.valueOf(responseBody.getOrDefault("link", ""));

            return WpPublishResult.success(
                    201,
                    remotePostId,
                    remotePostUrl,
                    responseBody == null ? null : responseBody.toString()
            );
        } catch (RestClientResponseException ex) {
            boolean retryable = ex.getStatusCode().is5xxServerError();
            return WpPublishResult.failure(
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString(),
                    retryable
            );
        } catch (RestClientException ex) {
            return WpPublishResult.failure(null, ex.getMessage(), true);
        }
    }

    private Long extractLong(Map<String, Object> responseBody, String key) {
        if (responseBody == null) {
            return null;
        }
        Object value = responseBody.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
