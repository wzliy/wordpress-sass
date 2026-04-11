package com.wpss.wordpresssass.catalog.interfaces;

import com.wpss.wordpresssass.catalog.application.AdminProductPublishApplicationService;
import com.wpss.wordpresssass.catalog.application.dto.SiteProductPublishDto;
import com.wpss.wordpresssass.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products/{productId}/publishes")
public class AdminProductPublishController {

    private final AdminProductPublishApplicationService adminProductPublishApplicationService;

    public AdminProductPublishController(AdminProductPublishApplicationService adminProductPublishApplicationService) {
        this.adminProductPublishApplicationService = adminProductPublishApplicationService;
    }

    @GetMapping
    public ApiResponse<List<SiteProductPublishDto>> list(@PathVariable("productId") Long productId) {
        return ApiResponse.success(adminProductPublishApplicationService.listPublishes(productId));
    }

    @PostMapping("/{siteId}/publish")
    public ApiResponse<SiteProductPublishDto> publishToSite(@PathVariable("productId") Long productId,
                                                            @PathVariable("siteId") Long siteId) {
        return ApiResponse.success(adminProductPublishApplicationService.publishToSite(productId, siteId));
    }

    @PostMapping("/{siteId}/unpublish")
    public ApiResponse<SiteProductPublishDto> unpublishFromSite(@PathVariable("productId") Long productId,
                                                                @PathVariable("siteId") Long siteId) {
        return ApiResponse.success(adminProductPublishApplicationService.unpublishFromSite(productId, siteId));
    }

    @PostMapping("/publish-all")
    public ApiResponse<List<SiteProductPublishDto>> publishToAllSites(@PathVariable("productId") Long productId) {
        return ApiResponse.success(adminProductPublishApplicationService.publishToAllSites(productId));
    }

    @PostMapping("/unpublish-all")
    public ApiResponse<List<SiteProductPublishDto>> unpublishFromAllSites(@PathVariable("productId") Long productId) {
        return ApiResponse.success(adminProductPublishApplicationService.unpublishFromAllSites(productId));
    }
}
