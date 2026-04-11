package com.wpss.wordpresssass.shipping.domain;

import java.util.List;
import java.util.Optional;

public interface ShipmentRecordRepository {

    ShipmentRecord save(ShipmentRecord shipmentRecord);

    Optional<ShipmentRecord> findByOrderId(Long tenantId, Long orderId);

    List<ShipmentRecordSummary> search(Long tenantId, String orderNo, String trackingNo, String customerEmail);

    void updateByOrderId(Long tenantId,
                         Long orderId,
                         ProcurementStatus procurementStatus,
                         ShipmentStatus shipmentStatus,
                         String trackingNo,
                         String carrier,
                         String failureReason);
}
