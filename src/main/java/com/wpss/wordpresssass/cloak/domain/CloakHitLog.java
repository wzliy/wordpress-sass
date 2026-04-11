package com.wpss.wordpresssass.cloak.domain;

import java.time.LocalDateTime;

public class CloakHitLog {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final Long ruleId;
    private final CloakResultType decision;
    private final String requestId;
    private final String requestSummaryJson;
    private final String matchedConditionJson;
    private final LocalDateTime createdAt;

    public CloakHitLog(Long id,
                       Long tenantId,
                       Long siteId,
                       Long ruleId,
                       CloakResultType decision,
                       String requestId,
                       String requestSummaryJson,
                       String matchedConditionJson,
                       LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.ruleId = ruleId;
        this.decision = decision;
        this.requestId = requestId;
        this.requestSummaryJson = requestSummaryJson;
        this.matchedConditionJson = matchedConditionJson;
        this.createdAt = createdAt;
    }

    public static CloakHitLog record(Long tenantId,
                                     Long siteId,
                                     Long ruleId,
                                     CloakResultType decision,
                                     String requestId,
                                     String requestSummaryJson,
                                     String matchedConditionJson) {
        return new CloakHitLog(
                null,
                tenantId,
                siteId,
                ruleId,
                decision,
                requestId,
                requestSummaryJson,
                matchedConditionJson,
                LocalDateTime.now()
        );
    }

    public CloakHitLog withId(Long newId) {
        return new CloakHitLog(
                newId,
                tenantId,
                siteId,
                ruleId,
                decision,
                requestId,
                requestSummaryJson,
                matchedConditionJson,
                createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public CloakResultType getDecision() {
        return decision;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRequestSummaryJson() {
        return requestSummaryJson;
    }

    public String getMatchedConditionJson() {
        return matchedConditionJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
