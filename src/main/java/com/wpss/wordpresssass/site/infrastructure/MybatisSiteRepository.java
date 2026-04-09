package com.wpss.wordpresssass.site.infrastructure;

import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteType;
import com.wpss.wordpresssass.site.domain.ProvisionStatus;
import com.wpss.wordpresssass.site.domain.SiteStatus;
import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.SiteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisSiteRepository implements SiteRepository {

    private static final int STATUS_MESSAGE_MAX_LENGTH = 255;

    private final SiteMapper siteMapper;

    public MybatisSiteRepository(SiteMapper siteMapper) {
        this.siteMapper = siteMapper;
    }

    @Override
    public Site save(Site site) {
        SiteDO siteDO = toDataObject(site);
        siteMapper.insert(siteDO);
        return toDomain(siteDO);
    }

    @Override
    public List<Site> findByTenantId(Long tenantId) {
        return siteMapper.selectByTenantId(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Site> findByIdAndTenantId(Long id, Long tenantId) {
        return siteMapper.selectByIdAndTenantId(id, tenantId)
                .map(this::toDomain);
    }

    @Override
    public void updateStatus(Long id, Long tenantId, SiteStatus status, String statusMessage) {
        siteMapper.updateStatus(id, tenantId, status.getCode(), truncateStatusMessage(statusMessage));
    }

    @Override
    public void updateProvisionResult(Site site) {
        siteMapper.updateProvisionResult(toDataObject(site));
    }

    private SiteDO toDataObject(Site site) {
        SiteDO siteDO = new SiteDO();
        siteDO.setId(site.getId());
        siteDO.setTenantId(site.getTenantId());
        siteDO.setName(site.getName());
        siteDO.setSiteType(site.getSiteType().name());
        siteDO.setBaseUrl(site.getBaseUrl());
        siteDO.setDomain(site.getDomain());
        siteDO.setAdminUrl(site.getAdminUrl());
        siteDO.setAuthType(site.getAuthType());
        siteDO.setWpUsername(site.getWpUsername());
        siteDO.setAppPassword(site.getAppPassword());
        siteDO.setStatus(site.getStatus().getCode());
        siteDO.setProvisionStatus(site.getProvisionStatus().name());
        siteDO.setStatusMsg(truncateStatusMessage(site.getStatusMessage()));
        siteDO.setCreatedAt(site.getCreatedAt());
        return siteDO;
    }

    private String truncateStatusMessage(String statusMessage) {
        if (statusMessage == null || statusMessage.length() <= STATUS_MESSAGE_MAX_LENGTH) {
            return statusMessage;
        }
        return statusMessage.substring(0, STATUS_MESSAGE_MAX_LENGTH);
    }

    private Site toDomain(SiteDO siteDO) {
        return new Site(
                siteDO.getId(),
                siteDO.getTenantId(),
                siteDO.getName(),
                SiteType.valueOf(siteDO.getSiteType()),
                siteDO.getBaseUrl(),
                siteDO.getDomain(),
                siteDO.getAdminUrl(),
                siteDO.getAuthType(),
                siteDO.getWpUsername(),
                siteDO.getAppPassword(),
                SiteStatus.fromCode(siteDO.getStatus()),
                ProvisionStatus.valueOf(siteDO.getProvisionStatus()),
                siteDO.getStatusMsg(),
                siteDO.getCreatedAt()
        );
    }
}
