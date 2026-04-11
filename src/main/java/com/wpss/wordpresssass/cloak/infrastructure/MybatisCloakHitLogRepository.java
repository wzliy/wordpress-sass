package com.wpss.wordpresssass.cloak.infrastructure;

import com.wpss.wordpresssass.cloak.domain.CloakHitLog;
import com.wpss.wordpresssass.cloak.domain.CloakHitLogRepository;
import com.wpss.wordpresssass.cloak.domain.CloakResultType;
import com.wpss.wordpresssass.cloak.infrastructure.dataobject.CloakHitLogDO;
import com.wpss.wordpresssass.cloak.infrastructure.mapper.CloakHitLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisCloakHitLogRepository implements CloakHitLogRepository {

    private final CloakHitLogMapper cloakHitLogMapper;

    public MybatisCloakHitLogRepository(CloakHitLogMapper cloakHitLogMapper) {
        this.cloakHitLogMapper = cloakHitLogMapper;
    }

    @Override
    public CloakHitLog save(CloakHitLog cloakHitLog) {
        CloakHitLogDO cloakHitLogDO = toDataObject(cloakHitLog);
        cloakHitLogMapper.insert(cloakHitLogDO);
        return toDomain(cloakHitLogDO);
    }

    @Override
    public List<CloakHitLog> findBySite(Long tenantId, Long siteId, int limit) {
        return cloakHitLogMapper.selectBySite(tenantId, siteId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CloakHitLogDO toDataObject(CloakHitLog cloakHitLog) {
        CloakHitLogDO cloakHitLogDO = new CloakHitLogDO();
        cloakHitLogDO.setId(cloakHitLog.getId());
        cloakHitLogDO.setTenantId(cloakHitLog.getTenantId());
        cloakHitLogDO.setSiteId(cloakHitLog.getSiteId());
        cloakHitLogDO.setRuleId(cloakHitLog.getRuleId());
        cloakHitLogDO.setDecision(cloakHitLog.getDecision().name());
        cloakHitLogDO.setRequestId(cloakHitLog.getRequestId());
        cloakHitLogDO.setRequestSummaryJson(cloakHitLog.getRequestSummaryJson());
        cloakHitLogDO.setMatchedConditionJson(cloakHitLog.getMatchedConditionJson());
        cloakHitLogDO.setCreatedAt(cloakHitLog.getCreatedAt());
        return cloakHitLogDO;
    }

    private CloakHitLog toDomain(CloakHitLogDO cloakHitLogDO) {
        return new CloakHitLog(
                cloakHitLogDO.getId(),
                cloakHitLogDO.getTenantId(),
                cloakHitLogDO.getSiteId(),
                cloakHitLogDO.getRuleId(),
                CloakResultType.valueOf(cloakHitLogDO.getDecision()),
                cloakHitLogDO.getRequestId(),
                cloakHitLogDO.getRequestSummaryJson(),
                cloakHitLogDO.getMatchedConditionJson(),
                cloakHitLogDO.getCreatedAt()
        );
    }
}
