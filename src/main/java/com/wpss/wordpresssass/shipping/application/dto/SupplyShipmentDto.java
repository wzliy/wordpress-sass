package com.wpss.wordpresssass.shipping.application.dto;

import com.wpss.wordpresssass.shipping.domain.ShipmentRecordSummary;

import java.time.LocalDateTime;

public record SupplyShipmentDto(
        Long orderId,
        Long siteId,
        String orderNo,
        String customerName,
        String customerEmail,
        String procurementStatus,
        String shipmentStatus,
        String trackingNo,
        String carrier,
        String failureReason,
        LocalDateTime updatedAt
) {

    public static SupplyShipmentDto from(ShipmentRecordSummary summary) {
        return new SupplyShipmentDto(
                summary.orderId(),
                summary.siteId(),
                summary.orderNo(),
                summary.customerName(),
                summary.customerEmail(),
                summary.procurementStatus().name(),
                summary.shipmentStatus().name(),
                summary.trackingNo(),
                summary.carrier(),
                summary.failureReason(),
                summary.updatedAt()
        );
    }
}
