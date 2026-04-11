package com.wpss.wordpresssass.shipping.domain;

import java.time.LocalDateTime;

public record ShipmentRecordSummary(
        Long orderId,
        Long siteId,
        String orderNo,
        String customerName,
        String customerEmail,
        ProcurementStatus procurementStatus,
        ShipmentStatus shipmentStatus,
        String trackingNo,
        String carrier,
        String failureReason,
        LocalDateTime updatedAt
) {
}
