package com.wpss.wordpresssass.ops.domain;

import java.time.LocalDateTime;

public class AuditLog {

    private final Long id;
    private final Long tenantId;
    private final Long operatorUserId;
    private final String operatorUsername;
    private final String moduleCode;
    private final String actionCode;
    private final String targetType;
    private final Long targetId;
    private final String targetName;
    private final String requestId;
    private final String beforeJson;
    private final String afterJson;
    private final AuditRiskLevel riskLevel;
    private final String ipAddress;
    private final String userAgent;
    private final String remark;
    private final LocalDateTime createdAt;

    public AuditLog(Long id, Long tenantId, Long operatorUserId, String operatorUsername, String moduleCode,
                    String actionCode, String targetType, Long targetId, String targetName, String requestId,
                    String beforeJson, String afterJson, AuditRiskLevel riskLevel, String ipAddress,
                    String userAgent, String remark, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.operatorUserId = operatorUserId;
        this.operatorUsername = operatorUsername;
        this.moduleCode = moduleCode;
        this.actionCode = actionCode;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.requestId = requestId;
        this.beforeJson = beforeJson;
        this.afterJson = afterJson;
        this.riskLevel = riskLevel;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public static AuditLog record(Long tenantId, Long operatorUserId, String operatorUsername, String moduleCode,
                                  String actionCode, String targetType, Long targetId, String targetName,
                                  String requestId, String beforeJson, String afterJson, AuditRiskLevel riskLevel,
                                  String ipAddress, String userAgent, String remark) {
        return new AuditLog(
                null,
                tenantId,
                operatorUserId,
                operatorUsername,
                moduleCode,
                actionCode,
                targetType,
                targetId,
                targetName,
                requestId,
                beforeJson,
                afterJson,
                riskLevel,
                ipAddress,
                userAgent,
                remark,
                LocalDateTime.now()
        );
    }

    public AuditLog withId(Long newId) {
        return new AuditLog(
                newId,
                tenantId,
                operatorUserId,
                operatorUsername,
                moduleCode,
                actionCode,
                targetType,
                targetId,
                targetName,
                requestId,
                beforeJson,
                afterJson,
                riskLevel,
                ipAddress,
                userAgent,
                remark,
                createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public String getOperatorUsername() {
        return operatorUsername;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getBeforeJson() {
        return beforeJson;
    }

    public String getAfterJson() {
        return afterJson;
    }

    public AuditRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getRemark() {
        return remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
