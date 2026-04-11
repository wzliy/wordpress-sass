package com.wpss.wordpresssass.site.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.site.application.SubsiteSettingsApplicationService;
import com.wpss.wordpresssass.site.application.command.UpdateSubsiteSettingsCommand;
import com.wpss.wordpresssass.site.application.dto.SubsiteSettingsDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subsite/settings")
public class SubsiteSettingsController {

    private final SubsiteSettingsApplicationService subsiteSettingsApplicationService;

    public SubsiteSettingsController(SubsiteSettingsApplicationService subsiteSettingsApplicationService) {
        this.subsiteSettingsApplicationService = subsiteSettingsApplicationService;
    }

    @GetMapping
    public ApiResponse<SubsiteSettingsDto> get(@RequestParam("siteId") Long siteId) {
        return ApiResponse.success(subsiteSettingsApplicationService.getSettings(siteId));
    }

    @PutMapping
    public ApiResponse<SubsiteSettingsDto> update(@Valid @RequestBody UpdateSubsiteSettingsCommand command) {
        return ApiResponse.success(subsiteSettingsApplicationService.updateSettings(command));
    }
}
