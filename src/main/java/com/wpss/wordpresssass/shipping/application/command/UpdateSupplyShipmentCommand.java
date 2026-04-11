package com.wpss.wordpresssass.shipping.application.command;

import com.wpss.wordpresssass.shipping.domain.ProcurementStatus;
import com.wpss.wordpresssass.shipping.domain.ShipmentStatus;

public record UpdateSupplyShipmentCommand(
        ProcurementStatus procurementStatus,
        ShipmentStatus shipmentStatus,
        String trackingNo,
        String carrier,
        String failureReason
) {
}
