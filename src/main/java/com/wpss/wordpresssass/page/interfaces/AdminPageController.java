package com.wpss.wordpresssass.page.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.page.application.AdminPageEditorApplicationService;
import com.wpss.wordpresssass.page.application.command.RollbackPageVersionCommand;
import com.wpss.wordpresssass.page.application.command.SavePageDraftCommand;
import com.wpss.wordpresssass.page.application.dto.PageEditorDto;
import com.wpss.wordpresssass.page.application.dto.PagePreviewDto;
import com.wpss.wordpresssass.page.application.dto.PagePublishDto;
import com.wpss.wordpresssass.page.application.dto.PageSummaryDto;
import com.wpss.wordpresssass.page.application.dto.PageVersionDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sites/{siteId}/pages")
public class AdminPageController {

    private final AdminPageEditorApplicationService adminPageEditorApplicationService;

    public AdminPageController(AdminPageEditorApplicationService adminPageEditorApplicationService) {
        this.adminPageEditorApplicationService = adminPageEditorApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PageSummaryDto>> list(@PathVariable("siteId") Long siteId) {
        return ApiResponse.success(adminPageEditorApplicationService.listPages(siteId));
    }

    @GetMapping("/{pageKey}/editor")
    public ApiResponse<PageEditorDto> editor(@PathVariable("siteId") Long siteId,
                                             @PathVariable("pageKey") String pageKey) {
        return ApiResponse.success(adminPageEditorApplicationService.getEditor(siteId, pageKey));
    }

    @GetMapping("/{pageKey}/versions")
    public ApiResponse<List<PageVersionDto>> versions(@PathVariable("siteId") Long siteId,
                                                      @PathVariable("pageKey") String pageKey) {
        return ApiResponse.success(adminPageEditorApplicationService.listVersions(siteId, pageKey));
    }

    @PutMapping("/{pageKey}/draft")
    public ApiResponse<PageEditorDto> saveDraft(@PathVariable("siteId") Long siteId,
                                                @PathVariable("pageKey") String pageKey,
                                                @Valid @RequestBody SavePageDraftCommand command) {
        return ApiResponse.success(adminPageEditorApplicationService.saveDraft(siteId, pageKey, command));
    }

    @PostMapping("/{pageKey}/preview")
    public ApiResponse<PagePreviewDto> preview(@PathVariable("siteId") Long siteId,
                                               @PathVariable("pageKey") String pageKey) {
        return ApiResponse.success(adminPageEditorApplicationService.preview(siteId, pageKey));
    }

    @PostMapping("/{pageKey}/publish")
    public ApiResponse<PagePublishDto> publish(@PathVariable("siteId") Long siteId,
                                               @PathVariable("pageKey") String pageKey) {
        return ApiResponse.success(adminPageEditorApplicationService.publish(siteId, pageKey));
    }

    @PostMapping("/{pageKey}/versions/{versionId}/rollback")
    public ApiResponse<PageEditorDto> rollback(@PathVariable("siteId") Long siteId,
                                               @PathVariable("pageKey") String pageKey,
                                               @PathVariable("versionId") Long versionId,
                                               @RequestBody(required = false) RollbackPageVersionCommand command) {
        return ApiResponse.success(adminPageEditorApplicationService.rollback(siteId, pageKey, versionId, command));
    }
}
