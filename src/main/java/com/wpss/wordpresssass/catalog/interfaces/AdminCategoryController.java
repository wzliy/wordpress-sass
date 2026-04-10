package com.wpss.wordpresssass.catalog.interfaces;

import com.wpss.wordpresssass.catalog.application.AdminCategoryApplicationService;
import com.wpss.wordpresssass.catalog.application.command.CreateCategoryCommand;
import com.wpss.wordpresssass.catalog.application.dto.CategoryDto;
import com.wpss.wordpresssass.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryApplicationService adminCategoryApplicationService;

    public AdminCategoryController(AdminCategoryApplicationService adminCategoryApplicationService) {
        this.adminCategoryApplicationService = adminCategoryApplicationService;
    }

    @PostMapping
    public ApiResponse<CategoryDto> create(@Valid @RequestBody CreateCategoryCommand command) {
        return ApiResponse.success(adminCategoryApplicationService.createCategory(command));
    }

    @GetMapping
    public ApiResponse<List<CategoryDto>> list() {
        return ApiResponse.success(adminCategoryApplicationService.listCategories());
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<CategoryDto> enable(@PathVariable("id") Long id) {
        return ApiResponse.success(adminCategoryApplicationService.enableCategory(id));
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<CategoryDto> disable(@PathVariable("id") Long id) {
        return ApiResponse.success(adminCategoryApplicationService.disableCategory(id));
    }
}
