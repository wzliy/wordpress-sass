package com.wpss.wordpresssass.site.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SiteWorkspaceDto(
        Long siteId,
        Long tenantId,
        String workspaceStatus,
        LocalDateTime generatedAt,
        Profile profile,
        Readiness readiness,
        List<ModuleSummary> moduleSummaries,
        List<PendingTask> pendingTasks,
        List<Alert> alerts,
        List<Activity> recentActivities,
        List<Action> quickActions
) {

    public record Profile(
            Long siteId,
            String name,
            String siteType,
            String domain,
            String baseUrl,
            String adminUrl,
            String status,
            String provisionStatus,
            String statusMessage,
            String templateCode,
            String templateName,
            String countryCode,
            String languageCode,
            String currencyCode,
            LocalDateTime createdAt,
            String createdBy
    ) {
    }

    public record Readiness(
            int score,
            String level,
            List<ReadinessItem> items
    ) {
    }

    public record ReadinessItem(
            String code,
            String label,
            String status,
            String message,
            String action
    ) {
    }

    public record ModuleSummary(
            String module,
            String status,
            String title,
            Metric primaryMetric,
            List<Metric> secondaryMetrics,
            List<String> highlights,
            List<Action> actions
    ) {
    }

    public record Metric(
            String label,
            String value,
            String tone
    ) {
    }

    public record PendingTask(
            Long taskId,
            String taskType,
            String status,
            String title,
            String message,
            LocalDateTime nextRunAt,
            LocalDateTime startedAt,
            int retryCount
    ) {
    }

    public record Alert(
            String level,
            String code,
            String title,
            String message,
            String action,
            LocalDateTime createdAt
    ) {
    }

    public record Activity(
            String type,
            String title,
            String description,
            String operatorName,
            LocalDateTime occurredAt,
            Long targetId,
            String targetType
    ) {
    }

    public record Action(
            String code,
            String label,
            String path,
            String type,
            boolean enabled
    ) {
    }
}
