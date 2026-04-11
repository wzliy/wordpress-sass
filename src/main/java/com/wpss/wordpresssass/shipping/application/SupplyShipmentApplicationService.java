package com.wpss.wordpresssass.shipping.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.order.domain.OrderRepository;
import com.wpss.wordpresssass.order.domain.ShippingStatus;
import com.wpss.wordpresssass.shipping.application.command.UpdateSupplyShipmentCommand;
import com.wpss.wordpresssass.shipping.application.dto.SupplyShipmentDto;
import com.wpss.wordpresssass.shipping.domain.ProcurementStatus;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecord;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecordRepository;
import com.wpss.wordpresssass.shipping.domain.ShipmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplyShipmentApplicationService {

    private final ShipmentRecordRepository shipmentRecordRepository;
    private final OrderRepository orderRepository;

    public SupplyShipmentApplicationService(ShipmentRecordRepository shipmentRecordRepository,
                                            OrderRepository orderRepository) {
        this.shipmentRecordRepository = shipmentRecordRepository;
        this.orderRepository = orderRepository;
    }

    public List<SupplyShipmentDto> search(String orderNo, String trackingNo, String customerEmail) {
        Long tenantId = requireTenantId();
        return shipmentRecordRepository.search(
                        tenantId,
                        normalize(orderNo),
                        normalize(trackingNo),
                        normalize(customerEmail)
                ).stream()
                .map(SupplyShipmentDto::from)
                .toList();
    }

    @Transactional
    public SupplyShipmentDto update(String orderNo, UpdateSupplyShipmentCommand command) {
        Long tenantId = requireTenantId();
        Order order = orderRepository.findByOrderNo(tenantId, requireOrderNo(orderNo))
                .orElseThrow(() -> new BusinessException("Order not found"));

        ShipmentRecord existing = shipmentRecordRepository.findByOrderId(tenantId, order.getId())
                .orElseGet(() -> shipmentRecordRepository.save(ShipmentRecord.createDefault(tenantId, order.getId())));

        ProcurementStatus procurementStatus = command.procurementStatus() == null
                ? existing.getProcurementStatus()
                : command.procurementStatus();
        ShipmentStatus shipmentStatus = command.shipmentStatus() == null
                ? existing.getShipmentStatus()
                : command.shipmentStatus();
        String trackingNo = mergeText(command.trackingNo(), existing.getTrackingNo());
        String carrier = mergeText(command.carrier(), existing.getCarrier());
        String failureReason = mergeText(command.failureReason(), existing.getFailureReason());

        shipmentRecordRepository.updateByOrderId(
                tenantId,
                order.getId(),
                procurementStatus,
                shipmentStatus,
                trackingNo,
                carrier,
                failureReason
        );
        orderRepository.updateShippingStatus(tenantId, order.getId(), ShippingStatus.valueOf(shipmentStatus.name()));

        return shipmentRecordRepository.search(tenantId, order.getOrderNo(), null, null)
                .stream()
                .findFirst()
                .map(SupplyShipmentDto::from)
                .orElseThrow(() -> new BusinessException("Shipment record not found"));
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }

    private String requireOrderNo(String orderNo) {
        String normalized = normalize(orderNo);
        if (normalized == null) {
            throw new BusinessException("orderNo is required");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String mergeText(String rawValue, String currentValue) {
        if (rawValue == null) {
            return currentValue;
        }
        String normalized = rawValue.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
