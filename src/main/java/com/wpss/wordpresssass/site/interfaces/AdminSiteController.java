package com.wpss.wordpresssass.site.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.site.application.AdminSiteApplicationService;
import com.wpss.wordpresssass.site.application.SiteApplicationService;
import com.wpss.wordpresssass.site.application.command.AddSiteCommand;
import com.wpss.wordpresssass.site.application.dto.AdminSiteDetailDto;
import com.wpss.wordpresssass.site.application.dto.SiteDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sites")
public class AdminSiteController {

    private final AdminSiteApplicationService adminSiteApplicationService;
    private final SiteApplicationService siteApplicationService;

    public AdminSiteController(AdminSiteApplicationService adminSiteApplicationService,
                               SiteApplicationService siteApplicationService) {
        this.adminSiteApplicationService = adminSiteApplicationService;
        this.siteApplicationService = siteApplicationService;
    }

    @PostMapping
    public ApiResponse<SiteDto> create(@Valid @RequestBody AddSiteCommand command) {
        return ApiResponse.success(siteApplicationService.registerSite(command));
    }

    @GetMapping
    public ApiResponse<List<SiteDto>> list() {
        return ApiResponse.success(adminSiteApplicationService.listSites());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminSiteDetailDto> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(adminSiteApplicationService.getSiteDetail(id));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<AdminSiteDetailDto> enable(@PathVariable("id") Long id) {
        return ApiResponse.success(adminSiteApplicationService.enableSite(id));
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<AdminSiteDetailDto> disable(@PathVariable("id") Long id) {
        return ApiResponse.success(adminSiteApplicationService.disableSite(id));
    }
}
