package com.wpss.wordpresssass.cloak.domain;

import java.time.LocalDateTime;

public class CloakRule {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final String ruleName;
    private final Integer priority;
    private final CloakRuleStatus status;
    private final CloakMatchMode matchMode;
    private final Integer trafficPercentage;
    private final String conditionJson;
    private final CloakResultType resultType;
    private final String resultJson;
    private final Integer versionNo;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CloakRule(Long id,
                     Long tenantId,
                     Long siteId,
                     String ruleName,
                     Integer priority,
                     CloakRuleStatus status,
                     CloakMatchMode matchMode,
                     Integer trafficPercentage,
                     String conditionJson,
                     CloakResultType resultType,
                     String resultJson,
                     Integer versionNo,
                     String createdBy,
                     LocalDateTime createdAt,
                     LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.ruleName = ruleName;
        this.priority = priority;
        this.status = status;
        this.matchMode = matchMode;
        this.trafficPercentage = trafficPercentage;
        this.conditionJson = conditionJson;
        this.resultType = resultType;
        this.resultJson = resultJson;
        this.versionNo = versionNo;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CloakRule createDraft(Long tenantId,
                                        Long siteId,
                                        String ruleName,
                                        Integer priority,
                                        CloakMatchMode matchMode,
                                        Integer trafficPercentage,
                                        String conditionJson,
                                        CloakResultType resultType,
                                        String resultJson,
                                        String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return new CloakRule(
                null,
                tenantId,
                siteId,
                ruleName,
                priority,
                CloakRuleStatus.DRAFT,
                matchMode,
                trafficPercentage,
                conditionJson,
                resultType,
                resultJson,
                1,
                createdBy,
                now,
                now
        );
    }

    public CloakRule withId(Long newId) {
        return new CloakRule(
                newId,
                tenantId,
                siteId,
                ruleName,
                priority,
                status,
                matchMode,
                trafficPercentage,
                conditionJson,
                resultType,
                resultJson,
                versionNo,
                createdBy,
                createdAt,
                updatedAt
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

    public String getRuleName() {
        return ruleName;
    }

    public Integer getPriority() {
        return priority;
    }

    public CloakRuleStatus getStatus() {
        return status;
    }

    public CloakMatchMode getMatchMode() {
        return matchMode;
    }

    public Integer getTrafficPercentage() {
        return trafficPercentage;
    }

    public String getConditionJson() {
        return conditionJson;
    }

    public CloakResultType getResultType() {
        return resultType;
    }

    public String getResultJson() {
        return resultJson;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
