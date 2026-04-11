package com.wpss.wordpresssass.shipping.domain;

import java.time.LocalDateTime;

public class ShipmentRecord {

    private final Long id;
    private final Long tenantId;
    private final Long orderId;
    private final ProcurementStatus procurementStatus;
    private final ShipmentStatus shipmentStatus;
    private final String trackingNo;
    private final String carrier;
    private final String failureReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ShipmentRecord(Long id,
                          Long tenantId,
                          Long orderId,
                          ProcurementStatus procurementStatus,
                          ShipmentStatus shipmentStatus,
                          String trackingNo,
                          String carrier,
                          String failureReason,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.procurementStatus = procurementStatus;
        this.shipmentStatus = shipmentStatus;
        this.trackingNo = trackingNo;
        this.carrier = carrier;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShipmentRecord createDefault(Long tenantId, Long orderId) {
        LocalDateTime now = LocalDateTime.now();
        return new ShipmentRecord(
                null,
                tenantId,
                orderId,
                ProcurementStatus.NOT_ORDERED,
                ShipmentStatus.NOT_SHIPPED,
                null,
                null,
                null,
                now,
                now
        );
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public ProcurementStatus getProcurementStatus() {
        return procurementStatus;
    }

    public ShipmentStatus getShipmentStatus() {
        return shipmentStatus;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
