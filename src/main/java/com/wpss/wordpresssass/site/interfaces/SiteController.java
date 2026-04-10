package com.wpss.wordpresssass.site.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.site.application.SiteApplicationService;
import com.wpss.wordpresssass.site.application.SiteTemplateApplicationService;
import com.wpss.wordpresssass.site.application.SiteWorkspaceApplicationService;
import com.wpss.wordpresssass.site.application.command.AddSiteCommand;
import com.wpss.wordpresssass.site.application.command.ProvisionSiteCommand;
import com.wpss.wordpresssass.site.application.dto.SiteConnectionResultDto;
import com.wpss.wordpresssass.site.application.dto.SiteDto;
import com.wpss.wordpresssass.site.application.dto.SiteProvisionResultDto;
import com.wpss.wordpresssass.site.application.dto.SiteTemplateDto;
import com.wpss.wordpresssass.site.application.dto.SiteWorkspaceDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/site")
public class SiteController {

    private final SiteApplicationService siteApplicationService;
    private final SiteTemplateApplicationService siteTemplateApplicationService;
    private final SiteWorkspaceApplicationService siteWorkspaceApplicationService;

    public SiteController(SiteApplicationService siteApplicationService,
                          SiteTemplateApplicationService siteTemplateApplicationService,
                          SiteWorkspaceApplicationService siteWorkspaceApplicationService) {
        this.siteApplicationService = siteApplicationService;
        this.siteTemplateApplicationService = siteTemplateApplicationService;
        this.siteWorkspaceApplicationService = siteWorkspaceApplicationService;
    }

    @PostMapping("/add")
    public ApiResponse<SiteDto> add(@Valid @RequestBody AddSiteCommand command) {
        return ApiResponse.success(siteApplicationService.addSite(command));
    }

    @PostMapping("/register")
    public ApiResponse<SiteDto> register(@Valid @RequestBody AddSiteCommand command) {
        return ApiResponse.success(siteApplicationService.registerSite(command));
    }

    @PostMapping("/provision")
    public ApiResponse<SiteProvisionResultDto> provision(@Valid @RequestBody ProvisionSiteCommand command) {
        return ApiResponse.success(siteApplicationService.provisionSite(command));
    }

    @GetMapping("/list")
    public ApiResponse<List<SiteDto>> list() {
        return ApiResponse.success(siteApplicationService.listSites());
    }

    @GetMapping("/template/list")
    public ApiResponse<List<SiteTemplateDto>> listTemplates() {
        return ApiResponse.success(siteTemplateApplicationService.listTemplates());
    }

    @GetMapping("/test")
    public ApiResponse<SiteConnectionResultDto> test(@RequestParam("id") Long id) {
        return ApiResponse.success(siteApplicationService.testConnection(id));
    }

    @GetMapping("/workspace")
    public ApiResponse<SiteWorkspaceDto> workspace(@RequestParam("id") Long id) {
        return ApiResponse.success(siteWorkspaceApplicationService.getWorkspace(id));
    }
}
