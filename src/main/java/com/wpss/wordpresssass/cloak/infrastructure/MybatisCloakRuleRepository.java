package com.wpss.wordpresssass.cloak.infrastructure;

import com.wpss.wordpresssass.cloak.domain.CloakMatchMode;
import com.wpss.wordpresssass.cloak.domain.CloakResultType;
import com.wpss.wordpresssass.cloak.domain.CloakRule;
import com.wpss.wordpresssass.cloak.domain.CloakRuleRepository;
import com.wpss.wordpresssass.cloak.domain.CloakRuleStatus;
import com.wpss.wordpresssass.cloak.infrastructure.dataobject.CloakRuleDO;
import com.wpss.wordpresssass.cloak.infrastructure.mapper.CloakRuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisCloakRuleRepository implements CloakRuleRepository {

    private final CloakRuleMapper cloakRuleMapper;

    public MybatisCloakRuleRepository(CloakRuleMapper cloakRuleMapper) {
        this.cloakRuleMapper = cloakRuleMapper;
    }

    @Override
    public CloakRule save(CloakRule cloakRule) {
        CloakRuleDO cloakRuleDO = toDataObject(cloakRule);
        cloakRuleMapper.insert(cloakRuleDO);
        return toDomain(cloakRuleDO);
    }

    @Override
    public void update(CloakRule cloakRule) {
        cloakRuleMapper.update(toDataObject(cloakRule));
    }

    @Override
    public Optional<CloakRule> findByIdAndTenantId(Long id, Long tenantId) {
        return cloakRuleMapper.selectByIdAndTenantId(id, tenantId)
                .map(this::toDomain);
    }

    @Override
    public List<CloakRule> findBySite(Long tenantId, Long siteId) {
        return cloakRuleMapper.selectBySite(tenantId, siteId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CloakRuleDO toDataObject(CloakRule cloakRule) {
        CloakRuleDO cloakRuleDO = new CloakRuleDO();
        cloakRuleDO.setId(cloakRule.getId());
        cloakRuleDO.setTenantId(cloakRule.getTenantId());
        cloakRuleDO.setSiteId(cloakRule.getSiteId());
        cloakRuleDO.setRuleName(cloakRule.getRuleName());
        cloakRuleDO.setPriority(cloakRule.getPriority());
        cloakRuleDO.setStatus(cloakRule.getStatus().name());
        cloakRuleDO.setMatchMode(cloakRule.getMatchMode().name());
        cloakRuleDO.setTrafficPercentage(cloakRule.getTrafficPercentage());
        cloakRuleDO.setConditionJson(cloakRule.getConditionJson());
        cloakRuleDO.setResultType(cloakRule.getResultType().name());
        cloakRuleDO.setResultJson(cloakRule.getResultJson());
        cloakRuleDO.setVersionNo(cloakRule.getVersionNo());
        cloakRuleDO.setCreatedBy(cloakRule.getCreatedBy());
        cloakRuleDO.setCreatedAt(cloakRule.getCreatedAt());
        cloakRuleDO.setUpdatedAt(cloakRule.getUpdatedAt());
        return cloakRuleDO;
    }

    private CloakRule toDomain(CloakRuleDO cloakRuleDO) {
        return new CloakRule(
                cloakRuleDO.getId(),
                cloakRuleDO.getTenantId(),
                cloakRuleDO.getSiteId(),
                cloakRuleDO.getRuleName(),
                cloakRuleDO.getPriority(),
                CloakRuleStatus.valueOf(cloakRuleDO.getStatus()),
                CloakMatchMode.valueOf(cloakRuleDO.getMatchMode()),
                cloakRuleDO.getTrafficPercentage(),
                cloakRuleDO.getConditionJson(),
                CloakResultType.valueOf(cloakRuleDO.getResultType()),
                cloakRuleDO.getResultJson(),
                cloakRuleDO.getVersionNo(),
                cloakRuleDO.getCreatedBy(),
                cloakRuleDO.getCreatedAt(),
                cloakRuleDO.getUpdatedAt()
        );
    }
}
