package com.wpss.wordpresssass.catalog.interfaces;

import com.wpss.wordpresssass.catalog.application.AdminProductApplicationService;
import com.wpss.wordpresssass.catalog.application.command.CreateProductCommand;
import com.wpss.wordpresssass.catalog.application.command.UpdateProductCommand;
import com.wpss.wordpresssass.catalog.application.dto.ProductDto;
import com.wpss.wordpresssass.common.api.ApiResponse;
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
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductApplicationService adminProductApplicationService;

    public AdminProductController(AdminProductApplicationService adminProductApplicationService) {
        this.adminProductApplicationService = adminProductApplicationService;
    }

    @PostMapping
    public ApiResponse<ProductDto> create(@Valid @RequestBody CreateProductCommand command) {
        return ApiResponse.success(adminProductApplicationService.createProduct(command));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDto> update(@PathVariable("id") Long id,
                                          @Valid @RequestBody UpdateProductCommand command) {
        return ApiResponse.success(adminProductApplicationService.updateProduct(id, command));
    }

    @GetMapping
    public ApiResponse<List<ProductDto>> list() {
        return ApiResponse.success(adminProductApplicationService.listProducts());
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<ProductDto> activate(@PathVariable("id") Long id) {
        return ApiResponse.success(adminProductApplicationService.activateProduct(id));
    }

    @PostMapping("/{id}/deactivate")
    public ApiResponse<ProductDto> deactivate(@PathVariable("id") Long id) {
        return ApiResponse.success(adminProductApplicationService.deactivateProduct(id));
    }
}
