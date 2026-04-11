package com.wpss.wordpresssass.report.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminOrderReportDto(
        Filters filters,
        Integer totalOrders,
        Integer paidOrders,
        Integer shippedOrders,
        BigDecimal totalRevenue,
        List<SiteSummary> siteSummaries
) {

    public record Filters(
            Long siteId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
    }

    public record SiteSummary(
            Long siteId,
            String siteName,
            Integer totalOrders,
            Integer paidOrders,
            Integer shippedOrders,
            BigDecimal totalRevenue
    ) {
    }
}
