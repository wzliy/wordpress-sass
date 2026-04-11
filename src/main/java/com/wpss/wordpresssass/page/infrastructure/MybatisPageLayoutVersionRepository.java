package com.wpss.wordpresssass.page.infrastructure;

import com.wpss.wordpresssass.page.domain.PageLayoutVersion;
import com.wpss.wordpresssass.page.domain.PageLayoutVersionRepository;
import com.wpss.wordpresssass.page.domain.PageVersionStatus;
import com.wpss.wordpresssass.page.infrastructure.dataobject.PageLayoutVersionDO;
import com.wpss.wordpresssass.page.infrastructure.mapper.PageLayoutVersionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisPageLayoutVersionRepository implements PageLayoutVersionRepository {

    private final PageLayoutVersionMapper pageLayoutVersionMapper;

    public MybatisPageLayoutVersionRepository(PageLayoutVersionMapper pageLayoutVersionMapper) {
        this.pageLayoutVersionMapper = pageLayoutVersionMapper;
    }

    @Override
    public PageLayoutVersion save(PageLayoutVersion version) {
        PageLayoutVersionDO versionDO = toDataObject(version);
        pageLayoutVersionMapper.insert(versionDO);
        return toDomain(versionDO);
    }

    @Override
    public void update(PageLayoutVersion version) {
        pageLayoutVersionMapper.update(toDataObject(version));
    }

    @Override
    public Optional<PageLayoutVersion> findByIdAndTenantId(Long id, Long tenantId) {
        return pageLayoutVersionMapper.selectByIdAndTenantId(id, tenantId)
                .map(this::toDomain);
    }

    @Override
    public List<PageLayoutVersion> findByPage(Long tenantId, Long siteId, Long pageId) {
        return pageLayoutVersionMapper.selectByPage(tenantId, siteId, pageId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PageLayoutVersionDO toDataObject(PageLayoutVersion version) {
        PageLayoutVersionDO versionDO = new PageLayoutVersionDO();
        versionDO.setId(version.getId());
        versionDO.setTenantId(version.getTenantId());
        versionDO.setSiteId(version.getSiteId());
        versionDO.setPageId(version.getPageId());
        versionDO.setVersionNo(version.getVersionNo());
        versionDO.setVersionStatus(version.getVersionStatus().name());
        versionDO.setSchemaVersion(version.getSchemaVersion());
        versionDO.setLayoutJson(version.getLayoutJson());
        versionDO.setCompiledRuntimeJson(version.getCompiledRuntimeJson());
        versionDO.setVersionNote(version.getVersionNote());
        versionDO.setCreatedBy(version.getCreatedBy());
        versionDO.setCreatedAt(version.getCreatedAt());
        versionDO.setPublishedAt(version.getPublishedAt());
        return versionDO;
    }

    private PageLayoutVersion toDomain(PageLayoutVersionDO versionDO) {
        return new PageLayoutVersion(
                versionDO.getId(),
                versionDO.getTenantId(),
                versionDO.getSiteId(),
                versionDO.getPageId(),
                versionDO.getVersionNo(),
                PageVersionStatus.valueOf(versionDO.getVersionStatus()),
                versionDO.getSchemaVersion(),
                versionDO.getLayoutJson(),
                versionDO.getCompiledRuntimeJson(),
                versionDO.getVersionNote(),
                versionDO.getCreatedBy(),
                versionDO.getCreatedAt(),
                versionDO.getPublishedAt()
        );
    }
}
