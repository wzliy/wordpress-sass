package com.wpss.wordpresssass.site.infrastructure;

import com.wpss.wordpresssass.site.domain.SiteDomain;
import com.wpss.wordpresssass.site.domain.SiteDomainRepository;
import com.wpss.wordpresssass.site.domain.SiteDomainStatus;
import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteDomainDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.SiteDomainMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisSiteDomainRepository implements SiteDomainRepository {

    private final SiteDomainMapper siteDomainMapper;

    public MybatisSiteDomainRepository(SiteDomainMapper siteDomainMapper) {
        this.siteDomainMapper = siteDomainMapper;
    }

    @Override
    public SiteDomain save(SiteDomain siteDomain) {
        SiteDomainDO siteDomainDO = toDataObject(siteDomain);
        siteDomainMapper.insert(siteDomainDO);
        return toDomain(siteDomainDO);
    }

    @Override
    public List<SiteDomain> findBySiteId(Long tenantId, Long siteId) {
        return siteDomainMapper.selectBySiteId(tenantId, siteId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<SiteDomain> findByDomain(String domain) {
        return siteDomainMapper.selectByDomain(domain).map(this::toDomain);
    }

    @Override
    public Optional<SiteDomain> findActiveByDomain(String domain) {
        return siteDomainMapper.selectActiveByDomain(domain).map(this::toDomain);
    }

    @Override
    public boolean existsByDomain(String domain) {
        return siteDomainMapper.selectByDomain(domain).isPresent();
    }

    @Override
    public int countBySiteId(Long tenantId, Long siteId) {
        return siteDomainMapper.countBySiteId(tenantId, siteId);
    }

    @Override
    public void clearPrimaryForSite(Long tenantId, Long siteId) {
        siteDomainMapper.clearPrimaryForSite(tenantId, siteId);
    }

    private SiteDomainDO toDataObject(SiteDomain siteDomain) {
        SiteDomainDO siteDomainDO = new SiteDomainDO();
        siteDomainDO.setId(siteDomain.getId());
        siteDomainDO.setTenantId(siteDomain.getTenantId());
        siteDomainDO.setSiteId(siteDomain.getSiteId());
        siteDomainDO.setDomain(siteDomain.getDomain());
        siteDomainDO.setIsPrimary(siteDomain.isPrimary() ? 1 : 0);
        siteDomainDO.setStatus(siteDomain.getStatus().name());
        siteDomainDO.setExpiryAt(siteDomain.getExpiryAt());
        siteDomainDO.setCreatedAt(siteDomain.getCreatedAt());
        return siteDomainDO;
    }

    private SiteDomain toDomain(SiteDomainDO siteDomainDO) {
        return new SiteDomain(
                siteDomainDO.getId(),
                siteDomainDO.getTenantId(),
                siteDomainDO.getSiteId(),
                siteDomainDO.getDomain(),
                siteDomainDO.getIsPrimary() != null && siteDomainDO.getIsPrimary() == 1,
                SiteDomainStatus.valueOf(siteDomainDO.getStatus()),
                siteDomainDO.getExpiryAt(),
                siteDomainDO.getCreatedAt()
        );
    }
}
