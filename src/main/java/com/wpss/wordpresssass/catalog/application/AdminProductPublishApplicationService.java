package com.wpss.wordpresssass.catalog.application;

import com.wpss.wordpresssass.catalog.application.dto.SiteProductPublishDto;
import com.wpss.wordpresssass.catalog.domain.Product;
import com.wpss.wordpresssass.catalog.domain.ProductRepository;
import com.wpss.wordpresssass.catalog.domain.SiteProductPublish;
import com.wpss.wordpresssass.catalog.domain.SiteProductPublishRepository;
import com.wpss.wordpresssass.catalog.domain.SiteProductPublishStatus;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminProductPublishApplicationService {

    private final ProductRepository productRepository;
    private final SiteRepository siteRepository;
    private final SiteProductPublishRepository siteProductPublishRepository;

    public AdminProductPublishApplicationService(ProductRepository productRepository,
                                                 SiteRepository siteRepository,
                                                 SiteProductPublishRepository siteProductPublishRepository) {
        this.productRepository = productRepository;
        this.siteRepository = siteRepository;
        this.siteProductPublishRepository = siteProductPublishRepository;
    }

    public List<SiteProductPublishDto> listPublishes(Long productId) {
        Long tenantId = requireTenantId();
        Product product = requireProduct(tenantId, productId);
        Map<Long, SiteProductPublish> publishBySiteId = siteProductPublishRepository.findByProductId(tenantId, productId)
                .stream()
                .collect(Collectors.toMap(SiteProductPublish::getSiteId, Function.identity()));
        return siteRepository.findByTenantId(tenantId)
                .stream()
                .map(site -> toDto(site, product, publishBySiteId.get(site.getId())))
                .toList();
    }

    public SiteProductPublishDto publishToSite(Long productId, Long siteId) {
        Long tenantId = requireTenantId();
        Product product = requireProduct(tenantId, productId);
        Site site = requireSite(tenantId, siteId);
        SiteProductPublish publish = siteProductPublishRepository.findBySiteIdAndProductId(tenantId, siteId, productId)
                .map(existing -> updateStatus(existing, SiteProductPublishStatus.PUBLISHED))
                .orElseGet(() -> siteProductPublishRepository.save(
                        SiteProductPublish.create(tenantId, siteId, productId, SiteProductPublishStatus.PUBLISHED)
                ));
        return toDto(site, product, publish);
    }

    public SiteProductPublishDto unpublishFromSite(Long productId, Long siteId) {
        Long tenantId = requireTenantId();
        Product product = requireProduct(tenantId, productId);
        Site site = requireSite(tenantId, siteId);
        SiteProductPublish publish = siteProductPublishRepository.findBySiteIdAndProductId(tenantId, siteId, productId)
                .map(existing -> updateStatus(existing, SiteProductPublishStatus.UNPUBLISHED))
                .orElse(null);
        return toDto(site, product, publish);
    }

    public List<SiteProductPublishDto> publishToAllSites(Long productId) {
        Long tenantId = requireTenantId();
        Product product = requireProduct(tenantId, productId);
        Map<Long, SiteProductPublish> existingBySiteId = siteProductPublishRepository.findByProductId(tenantId, productId)
                .stream()
                .collect(Collectors.toMap(SiteProductPublish::getSiteId, Function.identity()));
        return siteRepository.findByTenantId(tenantId)
                .stream()
                .map(site -> {
                    SiteProductPublish publish = existingBySiteId.containsKey(site.getId())
                            ? updateStatus(existingBySiteId.get(site.getId()), SiteProductPublishStatus.PUBLISHED)
                            : siteProductPublishRepository.save(
                                    SiteProductPublish.create(tenantId, site.getId(), productId, SiteProductPublishStatus.PUBLISHED)
                            );
                    return toDto(site, product, publish);
                })
                .toList();
    }

    public List<SiteProductPublishDto> unpublishFromAllSites(Long productId) {
        Long tenantId = requireTenantId();
        Product product = requireProduct(tenantId, productId);
        Map<Long, SiteProductPublish> existingBySiteId = siteProductPublishRepository.findByProductId(tenantId, productId)
                .stream()
                .collect(Collectors.toMap(SiteProductPublish::getSiteId, Function.identity()));
        return siteRepository.findByTenantId(tenantId)
                .stream()
                .map(site -> toDto(
                        site,
                        product,
                        existingBySiteId.containsKey(site.getId())
                                ? updateStatus(existingBySiteId.get(site.getId()), SiteProductPublishStatus.UNPUBLISHED)
                                : null
                ))
                .toList();
    }

    private SiteProductPublish updateStatus(SiteProductPublish existing, SiteProductPublishStatus targetStatus) {
        if (existing.getPublishStatus() == targetStatus) {
            return existing;
        }
        siteProductPublishRepository.updateStatus(existing.getId(), existing.getTenantId(), targetStatus);
        return existing.withStatus(targetStatus);
    }

    private Product requireProduct(Long tenantId, Long productId) {
        return productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException("Product not found"));
    }

    private Site requireSite(Long tenantId, Long siteId) {
        return siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
    }

    private SiteProductPublishDto toDto(Site site, Product product, SiteProductPublish publish) {
        return new SiteProductPublishDto(
                publish == null ? null : publish.getId(),
                site.getId(),
                site.getSiteCode(),
                site.getName(),
                site.getDomain(),
                site.getStatus().getCode(),
                product.getId(),
                product.getSku(),
                product.getTitle(),
                publish == null ? SiteProductPublishStatus.UNPUBLISHED.name() : publish.getPublishStatus().name(),
                publish == null ? null : publish.getCreatedAt(),
                publish == null ? null : publish.getUpdatedAt()
        );
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
