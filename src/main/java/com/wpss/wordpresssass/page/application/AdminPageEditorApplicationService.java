package com.wpss.wordpresssass.page.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.page.application.command.SavePageDraftCommand;
import com.wpss.wordpresssass.page.application.command.RollbackPageVersionCommand;
import com.wpss.wordpresssass.page.application.dto.PageEditorDto;
import com.wpss.wordpresssass.page.application.dto.PagePreviewDto;
import com.wpss.wordpresssass.page.application.dto.PagePublishDto;
import com.wpss.wordpresssass.page.application.dto.PageSummaryDto;
import com.wpss.wordpresssass.page.application.dto.PageVersionDto;
import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageLayoutVersion;
import com.wpss.wordpresssass.page.domain.PageLayoutVersionRepository;
import com.wpss.wordpresssass.page.domain.PageRepository;
import com.wpss.wordpresssass.page.domain.PageStatus;
import com.wpss.wordpresssass.page.domain.PageVersionStatus;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPageEditorApplicationService {

    private final SiteRepository siteRepository;
    private final SiteHomepageConfigRepository siteHomepageConfigRepository;
    private final PageRepository pageRepository;
    private final PageLayoutVersionRepository pageLayoutVersionRepository;
    private final PageRuntimeCompiler pageRuntimeCompiler;
    private final ObjectMapper objectMapper;

    public AdminPageEditorApplicationService(SiteRepository siteRepository,
                                             SiteHomepageConfigRepository siteHomepageConfigRepository,
                                             PageRepository pageRepository,
                                             PageLayoutVersionRepository pageLayoutVersionRepository,
                                             PageRuntimeCompiler pageRuntimeCompiler,
                                             ObjectMapper objectMapper) {
        this.siteRepository = siteRepository;
        this.siteHomepageConfigRepository = siteHomepageConfigRepository;
        this.pageRepository = pageRepository;
        this.pageLayoutVersionRepository = pageLayoutVersionRepository;
        this.pageRuntimeCompiler = pageRuntimeCompiler;
        this.objectMapper = objectMapper;
    }

    public List<PageSummaryDto> listPages(Long siteId) {
        Long tenantId = requireTenantId();
        requireSite(tenantId, siteId);
        return pageRepository.findBySite(tenantId, siteId)
                .stream()
                .map(PageSummaryDto::from)
                .toList();
    }

    public PageEditorDto getEditor(Long siteId, String pageKey) {
        Long tenantId = requireTenantId();
        Page page = requirePage(tenantId, siteId, pageKey);
        PageLayoutVersion currentVersion = requireCurrentVersion(tenantId, page);
        return toEditorDto(page, currentVersion);
    }

    public List<PageVersionDto> listVersions(Long siteId, String pageKey) {
        Long tenantId = requireTenantId();
        Page page = requirePage(tenantId, siteId, pageKey);
        return pageLayoutVersionRepository.findByPage(tenantId, siteId, page.getId())
                .stream()
                .map(version -> PageVersionDto.from(page, version))
                .toList();
    }

    public PageEditorDto saveDraft(Long siteId, String pageKey, SavePageDraftCommand command) {
        Long tenantId = requireTenantId();
        Page page = requirePage(tenantId, siteId, pageKey);
        JsonNode layout = requireValidLayout(command.layout(), page.getPageKey());
        String layoutJson = writeJson(layout);

        PageLayoutVersion currentVersion = requireCurrentVersion(tenantId, page);
        PageLayoutVersion draftVersion = currentVersion.getVersionStatus() == PageVersionStatus.DRAFT
                ? currentVersion.withUpdatedDraft(layoutJson, command.versionNote())
                : pageLayoutVersionRepository.save(PageLayoutVersion.draft(
                tenantId,
                siteId,
                page.getId(),
                nextVersionNo(tenantId, siteId, page.getId()),
                layoutJson,
                command.versionNote(),
                "system"
        ));

        if (currentVersion.getVersionStatus() == PageVersionStatus.DRAFT) {
            pageLayoutVersionRepository.update(draftVersion);
        }

        if (!draftVersion.getId().equals(page.getCurrentVersionId())) {
            PageStatus nextStatus = page.getPublishedVersionId() == null ? PageStatus.DRAFT_ONLY : page.getStatus();
            pageRepository.update(page.withVersionPointers(draftVersion.getId(), page.getPublishedVersionId(), nextStatus));
            page = pageRepository.findBySiteAndPageKey(tenantId, siteId, normalizePageKey(pageKey))
                    .orElseThrow(() -> new BusinessException("Page not found"));
        }

        return toEditorDto(page, draftVersion);
    }

    public PagePreviewDto preview(Long siteId, String pageKey) {
        Long tenantId = requireTenantId();
        Site site = requireSite(tenantId, siteId);
        Page page = requirePage(tenantId, siteId, pageKey);
        PageLayoutVersion currentVersion = requireCurrentVersion(tenantId, page);
        String runtimeJson = compileRuntime(site, page, currentVersion);
        return new PagePreviewDto(
                siteId,
                page.getId(),
                page.getPageKey(),
                currentVersion.getId(),
                currentVersion.getVersionStatus().name(),
                readJson(runtimeJson),
                LocalDateTime.now()
        );
    }

    public PagePublishDto publish(Long siteId, String pageKey) {
        Long tenantId = requireTenantId();
        Site site = requireSite(tenantId, siteId);
        Page page = requirePage(tenantId, siteId, pageKey);
        PageLayoutVersion currentVersion = requireCurrentVersion(tenantId, page);

        if (page.getPublishedVersionId() != null && !page.getPublishedVersionId().equals(currentVersion.getId())) {
            pageLayoutVersionRepository.findByIdAndTenantId(page.getPublishedVersionId(), tenantId)
                    .ifPresent(version -> pageLayoutVersionRepository.update(version.asArchived()));
        }

        String runtimeJson = compileRuntime(site, page, currentVersion);
        PageLayoutVersion publishedVersion = currentVersion.asPublished(runtimeJson, currentVersion.getVersionNote());
        pageLayoutVersionRepository.update(publishedVersion);
        if (Page.HOME_PAGE_KEY.equals(page.getPageKey())) {
            siteHomepageConfigRepository.saveOrUpdateConfig(tenantId, siteId, runtimeJson);
        }
        pageRepository.update(page.withVersionPointers(publishedVersion.getId(), publishedVersion.getId(), PageStatus.PUBLISHED));

        return new PagePublishDto(
                siteId,
                page.getId(),
                page.getPageKey(),
                publishedVersion.getId(),
                PageStatus.PUBLISHED.name(),
                PageVersionStatus.PUBLISHED.name(),
                readJson(runtimeJson),
                publishedVersion.getPublishedAt()
        );
    }

    public PageEditorDto rollback(Long siteId,
                                  String pageKey,
                                  Long versionId,
                                  RollbackPageVersionCommand command) {
        Long tenantId = requireTenantId();
        Page page = requirePage(tenantId, siteId, pageKey);
        PageLayoutVersion currentVersion = requireCurrentVersion(tenantId, page);
        PageLayoutVersion targetVersion = requirePageVersion(tenantId, page, versionId);

        if (currentVersion.getVersionStatus() == PageVersionStatus.DRAFT
                && !currentVersion.getId().equals(targetVersion.getId())) {
            pageLayoutVersionRepository.update(currentVersion.asArchived());
        }

        if (currentVersion.getId().equals(targetVersion.getId())
                && currentVersion.getVersionStatus() == PageVersionStatus.DRAFT) {
            return toEditorDto(page, currentVersion);
        }

        String versionNote = command == null || command.versionNote() == null || command.versionNote().isBlank()
                ? "Rollback from version v" + targetVersion.getVersionNo()
                : command.versionNote().trim();
        PageLayoutVersion rollbackDraft = pageLayoutVersionRepository.save(
                PageLayoutVersion.rollbackDraft(
                        tenantId,
                        siteId,
                        page.getId(),
                        nextVersionNo(tenantId, siteId, page.getId()),
                        targetVersion.getLayoutJson(),
                        versionNote,
                        "system"
                )
        );
        pageRepository.update(page.withVersionPointers(
                rollbackDraft.getId(),
                page.getPublishedVersionId(),
                page.getPublishedVersionId() == null ? PageStatus.DRAFT_ONLY : page.getStatus()
        ));
        Page refreshedPage = pageRepository.findBySiteAndPageKey(tenantId, siteId, normalizePageKey(pageKey))
                .orElseThrow(() -> new BusinessException("Page not found"));
        return toEditorDto(refreshedPage, rollbackDraft);
    }

    private int nextVersionNo(Long tenantId, Long siteId, Long pageId) {
        return pageLayoutVersionRepository.findByPage(tenantId, siteId, pageId)
                .stream()
                .map(PageLayoutVersion::getVersionNo)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private Page requirePage(Long tenantId, Long siteId, String pageKey) {
        requireSite(tenantId, siteId);
        return pageRepository.findBySiteAndPageKey(tenantId, siteId, normalizePageKey(pageKey))
                .orElseThrow(() -> new BusinessException("Page not found"));
    }

    private Site requireSite(Long tenantId, Long siteId) {
        return siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
    }

    private PageLayoutVersion requireCurrentVersion(Long tenantId, Page page) {
        Long versionId = page.getCurrentVersionId() != null ? page.getCurrentVersionId() : page.getPublishedVersionId();
        if (versionId == null) {
            throw new BusinessException("Page version not found");
        }
        return pageLayoutVersionRepository.findByIdAndTenantId(versionId, tenantId)
                .orElseThrow(() -> new BusinessException("Page version not found"));
    }

    private JsonNode requireValidLayout(JsonNode layout, String expectedPageKey) {
        if (layout == null || !layout.isObject()) {
            throw new BusinessException("layout must be a JSON object");
        }
        String pageKey = layout.path("pageKey").asText(null);
        if (pageKey == null || !normalizePageKey(pageKey).equals(expectedPageKey)) {
            throw new BusinessException("layout.pageKey does not match target page");
        }
        if (!layout.path("sections").isArray()) {
            throw new BusinessException("layout.sections must be an array");
        }
        return layout;
    }

    private String normalizePageKey(String pageKey) {
        return pageKey == null ? "" : pageKey.trim().toUpperCase();
    }

    private String compileRuntime(Site site, Page page, PageLayoutVersion version) {
        String baseRuntimeJson = siteHomepageConfigRepository.findBySite(site.getTenantId(), site.getId())
                .map(config -> config.getConfigJson())
                .orElse("{}");
        return pageRuntimeCompiler.write(pageRuntimeCompiler.compile(page.getPageKey(), version.getLayoutJson(), site, baseRuntimeJson));
    }

    private PageLayoutVersion requirePageVersion(Long tenantId, Page page, Long versionId) {
        PageLayoutVersion version = pageLayoutVersionRepository.findByIdAndTenantId(versionId, tenantId)
                .orElseThrow(() -> new BusinessException("Page version not found"));
        if (!version.getPageId().equals(page.getId())) {
            throw new BusinessException("Page version not found");
        }
        return version;
    }

    private PageEditorDto toEditorDto(Page page, PageLayoutVersion currentVersion) {
        return new PageEditorDto(
                page.getSiteId(),
                page.getId(),
                page.getPageKey(),
                page.getPageName(),
                page.getPageType().name(),
                page.getStatus().name(),
                page.getCurrentVersionId(),
                page.getPublishedVersionId(),
                currentVersion.getVersionNo(),
                currentVersion.getVersionStatus().name(),
                currentVersion.getCreatedAt(),
                readJson(currentVersion.getLayoutJson()),
                blockLibrary(page.getPageKey())
        );
    }

    private List<PageEditorDto.BlockSchemaDto> blockLibrary(String pageKey) {
        return switch (normalizePageKey(pageKey)) {
            case Page.PRODUCT_PAGE_KEY -> List.of(
                    new PageEditorDto.BlockSchemaDto(
                            "rich-text",
                            "Rich Text",
                            "Content",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", true),
                                    new PageEditorDto.FieldSchemaDto("body", "正文", "rich-text", false)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "trust-badges",
                            "Trust Badges",
                            "Conversion",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("items", "信任标签", "list", true)
                            )
                    )
            );
            case Page.CHECKOUT_PAGE_KEY -> List.of(
                    new PageEditorDto.BlockSchemaDto(
                            "checkout-notice",
                            "Checkout Notice",
                            "Checkout",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", true),
                                    new PageEditorDto.FieldSchemaDto("body", "说明", "textarea", false),
                                    new PageEditorDto.FieldSchemaDto("submitLabel", "提交按钮", "text", false)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "trust-badges",
                            "Trust Badges",
                            "Conversion",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("items", "信任标签", "list", true)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "rich-text",
                            "Rich Text",
                            "Content",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", false),
                                    new PageEditorDto.FieldSchemaDto("body", "正文", "rich-text", false)
                            )
                    )
            );
            case Page.SUCCESS_PAGE_KEY -> List.of(
                    new PageEditorDto.BlockSchemaDto(
                            "hero-banner",
                            "Hero Banner",
                            "Hero",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", true),
                                    new PageEditorDto.FieldSchemaDto("subtitle", "副标题", "textarea", false),
                                    new PageEditorDto.FieldSchemaDto("ctaLabel", "按钮文案", "text", false),
                                    new PageEditorDto.FieldSchemaDto("ctaPath", "按钮链接", "text", false)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "order-next-steps",
                            "Order Next Steps",
                            "Success",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", true),
                                    new PageEditorDto.FieldSchemaDto("items", "步骤列表", "list", true)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "rich-text",
                            "Rich Text",
                            "Content",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", false),
                                    new PageEditorDto.FieldSchemaDto("body", "正文", "rich-text", false)
                            )
                    )
            );
            default -> List.of(
                    new PageEditorDto.BlockSchemaDto(
                            "hero-banner",
                            "Hero Banner",
                            "Hero",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", true),
                                    new PageEditorDto.FieldSchemaDto("subtitle", "副标题", "textarea", false),
                                    new PageEditorDto.FieldSchemaDto("ctaLabel", "按钮文案", "text", false),
                                    new PageEditorDto.FieldSchemaDto("ctaPath", "按钮链接", "text", false)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "top-menu",
                            "Top Menu",
                            "Navigation",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("items", "菜单项", "list", true)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "featured-products",
                            "Featured Products",
                            "Catalog",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("productIds", "商品列表", "product-picker", true)
                            )
                    ),
                    new PageEditorDto.BlockSchemaDto(
                            "rich-text",
                            "Rich Text",
                            "Content",
                            List.of(
                                    new PageEditorDto.FieldSchemaDto("title", "标题", "text", false),
                                    new PageEditorDto.FieldSchemaDto("body", "正文", "rich-text", false)
                            )
                    )
            );
        };
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to parse page layout");
        }
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize page layout");
        }
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
