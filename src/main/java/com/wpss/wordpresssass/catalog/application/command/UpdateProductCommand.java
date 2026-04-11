package com.wpss.wordpresssass.catalog.application.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductCommand(
        @NotBlank(message = "must not be blank")
        String sku,
        @NotBlank(message = "must not be blank")
        String title,
        @NotNull(message = "must not be null")
        Long categoryId,
        String coverImage,
        List<String> galleryImages,
        String descriptionHtml,
        List<String> sizes,
        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.00", message = "must be greater than or equal to 0")
        BigDecimal price,
        @DecimalMin(value = "0.00", message = "must be greater than or equal to 0")
        BigDecimal compareAtPrice,
        String status
) {
}
