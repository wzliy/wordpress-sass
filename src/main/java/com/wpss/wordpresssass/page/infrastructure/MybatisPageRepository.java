package com.wpss.wordpresssass.page.infrastructure;

import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageRepository;
import com.wpss.wordpresssass.page.domain.PageStatus;
import com.wpss.wordpresssass.page.domain.PageType;
import com.wpss.wordpresssass.page.infrastructure.dataobject.PageDO;
import com.wpss.wordpresssass.page.infrastructure.mapper.PageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisPageRepository implements PageRepository {

    private final PageMapper pageMapper;

    public MybatisPageRepository(PageMapper pageMapper) {
        this.pageMapper = pageMapper;
    }

    @Override
    public Page save(Page page) {
        PageDO pageDO = toDataObject(page);
        pageMapper.insert(pageDO);
        return toDomain(pageDO);
    }

    @Override
    public void update(Page page) {
        pageMapper.update(toDataObject(page));
    }

    @Override
    public Optional<Page> findBySiteAndPageKey(Long tenantId, Long siteId, String pageKey) {
        return pageMapper.selectBySiteAndPageKey(tenantId, siteId, pageKey)
                .map(this::toDomain);
    }

    @Override
    public List<Page> findBySite(Long tenantId, Long siteId) {
        return pageMapper.selectBySite(tenantId, siteId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PageDO toDataObject(Page page) {
        PageDO pageDO = new PageDO();
        pageDO.setId(page.getId());
        pageDO.setTenantId(page.getTenantId());
        pageDO.setSiteId(page.getSiteId());
        pageDO.setPageKey(page.getPageKey());
        pageDO.setPageName(page.getPageName());
        pageDO.setPageType(page.getPageType().name());
        pageDO.setStatus(page.getStatus().name());
        pageDO.setCurrentVersionId(page.getCurrentVersionId());
        pageDO.setPublishedVersionId(page.getPublishedVersionId());
        pageDO.setCreatedAt(page.getCreatedAt());
        pageDO.setUpdatedAt(page.getUpdatedAt());
        return pageDO;
    }

    private Page toDomain(PageDO pageDO) {
        return new Page(
                pageDO.getId(),
                pageDO.getTenantId(),
                pageDO.getSiteId(),
                pageDO.getPageKey(),
                pageDO.getPageName(),
                PageType.valueOf(pageDO.getPageType()),
                PageStatus.valueOf(pageDO.getStatus()),
                pageDO.getCurrentVersionId(),
                pageDO.getPublishedVersionId(),
                pageDO.getCreatedAt(),
                pageDO.getUpdatedAt()
        );
    }
}
