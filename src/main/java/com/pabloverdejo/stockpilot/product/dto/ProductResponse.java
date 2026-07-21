package com.pabloverdejo.stockpilot.product.dto;

import com.pabloverdejo.stockpilot.product.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal unitPrice,
        int currentStock,
        int reorderLevel,
        boolean lowStock,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getCurrentStock(),
                product.getReorderLevel(),
                product.getCurrentStock() <= product.getReorderLevel(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
