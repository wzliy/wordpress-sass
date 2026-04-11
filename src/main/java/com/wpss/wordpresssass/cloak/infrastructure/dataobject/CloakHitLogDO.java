package com.wpss.wordpresssass.cloak.infrastructure.dataobject;

import java.time.LocalDateTime;

public class CloakHitLogDO {

    private Long id;
    private Long tenantId;
    private Long siteId;
    private Long ruleId;
    private String decision;
    private String requestId;
    private String requestSummaryJson;
    private String matchedConditionJson;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestSummaryJson() {
        return requestSummaryJson;
    }

    public void setRequestSummaryJson(String requestSummaryJson) {
        this.requestSummaryJson = requestSummaryJson;
    }

    public String getMatchedConditionJson() {
        return matchedConditionJson;
    }

    public void setMatchedConditionJson(String matchedConditionJson) {
        this.matchedConditionJson = matchedConditionJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
