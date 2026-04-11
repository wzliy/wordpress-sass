package com.wpss.wordpresssass.order.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.order.application.dto.SubsiteOrderDto;
import com.wpss.wordpresssass.order.domain.OrderRepository;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SubsiteOrderApplicationService {

    private final SiteRepository siteRepository;
    private final OrderRepository orderRepository;

    public SubsiteOrderApplicationService(SiteRepository siteRepository,
                                          OrderRepository orderRepository) {
        this.siteRepository = siteRepository;
        this.orderRepository = orderRepository;
    }

    public List<SubsiteOrderDto> listOrders(Long siteId,
                                            String orderNo,
                                            String orderStatus,
                                            String paymentStatus,
                                            LocalDate createdFrom,
                                            LocalDate createdTo) {
        Long tenantId = requireTenantId();
        siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
        return orderRepository.findBySite(
                        tenantId,
                        siteId,
                        normalize(orderNo),
                        normalize(orderStatus),
                        normalize(paymentStatus),
                        createdFrom == null ? null : createdFrom.atStartOfDay(),
                        createdTo == null ? null : LocalDateTime.of(createdTo, LocalTime.MAX)
                ).stream()
                .map(SubsiteOrderDto::from)
                .toList();
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
