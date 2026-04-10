package com.wpss.wordpresssass.site.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.site.application.SiteDomainApplicationService;
import com.wpss.wordpresssass.site.application.command.BindSiteDomainCommand;
import com.wpss.wordpresssass.site.application.dto.SiteDomainDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/domains")
public class AdminDomainController {

    private final SiteDomainApplicationService siteDomainApplicationService;

    public AdminDomainController(SiteDomainApplicationService siteDomainApplicationService) {
        this.siteDomainApplicationService = siteDomainApplicationService;
    }

    @PostMapping
    public ApiResponse<SiteDomainDto> bind(@Valid @RequestBody BindSiteDomainCommand command) {
        return ApiResponse.success(siteDomainApplicationService.bindDomain(command));
    }

    @GetMapping
    public ApiResponse<List<SiteDomainDto>> list(@RequestParam("siteId") Long siteId) {
        return ApiResponse.success(siteDomainApplicationService.listDomains(siteId));
    }
}
