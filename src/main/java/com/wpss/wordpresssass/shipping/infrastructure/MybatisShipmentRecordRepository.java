package com.wpss.wordpresssass.shipping.infrastructure;

import com.wpss.wordpresssass.shipping.domain.ProcurementStatus;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecord;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecordRepository;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecordSummary;
import com.wpss.wordpresssass.shipping.domain.ShipmentStatus;
import com.wpss.wordpresssass.shipping.infrastructure.dataobject.ShipmentRecordDO;
import com.wpss.wordpresssass.shipping.infrastructure.dataobject.ShipmentRecordSummaryDO;
import com.wpss.wordpresssass.shipping.infrastructure.mapper.ShipmentRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisShipmentRecordRepository implements ShipmentRecordRepository {

    private final ShipmentRecordMapper shipmentRecordMapper;

    public MybatisShipmentRecordRepository(ShipmentRecordMapper shipmentRecordMapper) {
        this.shipmentRecordMapper = shipmentRecordMapper;
    }

    @Override
    public ShipmentRecord save(ShipmentRecord shipmentRecord) {
        ShipmentRecordDO shipmentRecordDO = toDataObject(shipmentRecord);
        shipmentRecordMapper.insert(shipmentRecordDO);
        return toDomain(shipmentRecordDO);
    }

    @Override
    public Optional<ShipmentRecord> findByOrderId(Long tenantId, Long orderId) {
        return shipmentRecordMapper.selectByOrderId(tenantId, orderId)
                .map(this::toDomain);
    }

    @Override
    public List<ShipmentRecordSummary> search(Long tenantId, String orderNo, String trackingNo, String customerEmail) {
        return shipmentRecordMapper.search(tenantId, orderNo, trackingNo, customerEmail)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public void updateByOrderId(Long tenantId,
                                Long orderId,
                                ProcurementStatus procurementStatus,
                                ShipmentStatus shipmentStatus,
                                String trackingNo,
                                String carrier,
                                String failureReason) {
        shipmentRecordMapper.updateByOrderId(
                tenantId,
                orderId,
                procurementStatus.name(),
                shipmentStatus.name(),
                trackingNo,
                carrier,
                failureReason
        );
    }

    private ShipmentRecordDO toDataObject(ShipmentRecord shipmentRecord) {
        ShipmentRecordDO shipmentRecordDO = new ShipmentRecordDO();
        shipmentRecordDO.setId(shipmentRecord.getId());
        shipmentRecordDO.setTenantId(shipmentRecord.getTenantId());
        shipmentRecordDO.setOrderId(shipmentRecord.getOrderId());
        shipmentRecordDO.setProcurementStatus(shipmentRecord.getProcurementStatus().name());
        shipmentRecordDO.setShipmentStatus(shipmentRecord.getShipmentStatus().name());
        shipmentRecordDO.setTrackingNo(shipmentRecord.getTrackingNo());
        shipmentRecordDO.setCarrier(shipmentRecord.getCarrier());
        shipmentRecordDO.setFailureReason(shipmentRecord.getFailureReason());
        shipmentRecordDO.setCreatedAt(shipmentRecord.getCreatedAt());
        shipmentRecordDO.setUpdatedAt(shipmentRecord.getUpdatedAt());
        return shipmentRecordDO;
    }

    private ShipmentRecord toDomain(ShipmentRecordDO shipmentRecordDO) {
        return new ShipmentRecord(
                shipmentRecordDO.getId(),
                shipmentRecordDO.getTenantId(),
                shipmentRecordDO.getOrderId(),
                ProcurementStatus.fromValue(shipmentRecordDO.getProcurementStatus()),
                ShipmentStatus.fromValue(shipmentRecordDO.getShipmentStatus()),
                shipmentRecordDO.getTrackingNo(),
                shipmentRecordDO.getCarrier(),
                shipmentRecordDO.getFailureReason(),
                shipmentRecordDO.getCreatedAt(),
                shipmentRecordDO.getUpdatedAt()
        );
    }

    private ShipmentRecordSummary toSummary(ShipmentRecordSummaryDO summaryDO) {
        return new ShipmentRecordSummary(
                summaryDO.getOrderId(),
                summaryDO.getSiteId(),
                summaryDO.getOrderNo(),
                summaryDO.getCustomerName(),
                summaryDO.getCustomerEmail(),
                ProcurementStatus.fromValue(summaryDO.getProcurementStatus()),
                ShipmentStatus.fromValue(summaryDO.getShipmentStatus()),
                summaryDO.getTrackingNo(),
                summaryDO.getCarrier(),
                summaryDO.getFailureReason(),
                summaryDO.getUpdatedAt()
        );
    }
}
