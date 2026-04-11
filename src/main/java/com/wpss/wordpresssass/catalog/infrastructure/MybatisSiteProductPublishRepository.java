package com.wpss.wordpresssass.catalog.infrastructure;

import com.wpss.wordpresssass.catalog.domain.SiteProductPublish;
import com.wpss.wordpresssass.catalog.domain.SiteProductPublishRepository;
import com.wpss.wordpresssass.catalog.domain.SiteProductPublishStatus;
import com.wpss.wordpresssass.catalog.infrastructure.dataobject.SiteProductPublishDO;
import com.wpss.wordpresssass.catalog.infrastructure.mapper.SiteProductPublishMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisSiteProductPublishRepository implements SiteProductPublishRepository {

    private final SiteProductPublishMapper siteProductPublishMapper;

    public MybatisSiteProductPublishRepository(SiteProductPublishMapper siteProductPublishMapper) {
        this.siteProductPublishMapper = siteProductPublishMapper;
    }

    @Override
    public SiteProductPublish save(SiteProductPublish siteProductPublish) {
        SiteProductPublishDO siteProductPublishDO = toDataObject(siteProductPublish);
        siteProductPublishMapper.insert(siteProductPublishDO);
        return toDomain(siteProductPublishDO);
    }

    @Override
    public Optional<SiteProductPublish> findBySiteIdAndProductId(Long tenantId, Long siteId, Long productId) {
        return siteProductPublishMapper.selectBySiteIdAndProductId(tenantId, siteId, productId)
                .map(this::toDomain);
    }

    @Override
    public List<SiteProductPublish> findByProductId(Long tenantId, Long productId) {
        return siteProductPublishMapper.selectByProductId(tenantId, productId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateStatus(Long id, Long tenantId, SiteProductPublishStatus status) {
        siteProductPublishMapper.updateStatus(id, tenantId, status.name());
    }

    private SiteProductPublishDO toDataObject(SiteProductPublish siteProductPublish) {
        SiteProductPublishDO siteProductPublishDO = new SiteProductPublishDO();
        siteProductPublishDO.setId(siteProductPublish.getId());
        siteProductPublishDO.setTenantId(siteProductPublish.getTenantId());
        siteProductPublishDO.setSiteId(siteProductPublish.getSiteId());
        siteProductPublishDO.setProductId(siteProductPublish.getProductId());
        siteProductPublishDO.setPublishStatus(siteProductPublish.getPublishStatus().name());
        siteProductPublishDO.setCreatedAt(siteProductPublish.getCreatedAt());
        siteProductPublishDO.setUpdatedAt(siteProductPublish.getUpdatedAt());
        return siteProductPublishDO;
    }

    private SiteProductPublish toDomain(SiteProductPublishDO siteProductPublishDO) {
        return new SiteProductPublish(
                siteProductPublishDO.getId(),
                siteProductPublishDO.getTenantId(),
                siteProductPublishDO.getSiteId(),
                siteProductPublishDO.getProductId(),
                SiteProductPublishStatus.valueOf(siteProductPublishDO.getPublishStatus()),
                siteProductPublishDO.getCreatedAt(),
                siteProductPublishDO.getUpdatedAt()
        );
    }
}
