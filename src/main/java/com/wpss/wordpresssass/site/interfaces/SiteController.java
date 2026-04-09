package com.wpss.wordpresssass.site.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.site.application.SiteApplicationService;
import com.wpss.wordpresssass.site.application.command.AddSiteCommand;
import com.wpss.wordpresssass.site.application.command.ProvisionSiteCommand;
import com.wpss.wordpresssass.site.application.dto.SiteConnectionResultDto;
import com.wpss.wordpresssass.site.application.dto.SiteDto;
import com.wpss.wordpresssass.site.application.dto.SiteProvisionResultDto;
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

    public SiteController(SiteApplicationService siteApplicationService) {
        this.siteApplicationService = siteApplicationService;
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

    @GetMapping("/test")
    public ApiResponse<SiteConnectionResultDto> test(@RequestParam("id") Long id) {
        return ApiResponse.success(siteApplicationService.testConnection(id));
    }
}
