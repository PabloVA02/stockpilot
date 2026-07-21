package com.pabloverdejo.stockpilot.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(max = 40)
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "must contain only letters, numbers, underscores or dashes")
        String sku,

        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @Min(0) int initialStock,
        @Min(0) int reorderLevel) {
}
