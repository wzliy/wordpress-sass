package com.wpss.wordpresssass.report.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.report.application.dto.AdminOrderReportDto;
import com.wpss.wordpresssass.report.infrastructure.dataobject.SiteOrderSummaryDO;
import com.wpss.wordpresssass.report.infrastructure.mapper.OrderReportMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AdminReportApplicationService {

    private final OrderReportMapper orderReportMapper;

    public AdminReportApplicationService(OrderReportMapper orderReportMapper) {
        this.orderReportMapper = orderReportMapper;
    }

    public AdminOrderReportDto overview(Long siteId, LocalDate dateFrom, LocalDate dateTo) {
        Long tenantId = requireTenantId();
        List<SiteOrderSummaryDO> summaries = orderReportMapper.summarizeBySite(
                tenantId,
                siteId,
                dateFrom == null ? null : dateFrom.atStartOfDay(),
                dateTo == null ? null : LocalDateTime.of(dateTo, LocalTime.MAX)
        );

        int totalOrders = 0;
        int paidOrders = 0;
        int shippedOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (SiteOrderSummaryDO summary : summaries) {
            totalOrders += safeInt(summary.getTotalOrders());
            paidOrders += safeInt(summary.getPaidOrders());
            shippedOrders += safeInt(summary.getShippedOrders());
            totalRevenue = totalRevenue.add(safeDecimal(summary.getTotalRevenue()));
        }

        return new AdminOrderReportDto(
                new AdminOrderReportDto.Filters(siteId, dateFrom, dateTo),
                totalOrders,
                paidOrders,
                shippedOrders,
                totalRevenue,
                summaries.stream()
                        .map(this::toSiteSummary)
                        .toList()
        );
    }

    private AdminOrderReportDto.SiteSummary toSiteSummary(SiteOrderSummaryDO summary) {
        return new AdminOrderReportDto.SiteSummary(
                summary.getSiteId(),
                summary.getSiteName(),
                safeInt(summary.getTotalOrders()),
                safeInt(summary.getPaidOrders()),
                safeInt(summary.getShippedOrders()),
                safeDecimal(summary.getTotalRevenue())
        );
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
