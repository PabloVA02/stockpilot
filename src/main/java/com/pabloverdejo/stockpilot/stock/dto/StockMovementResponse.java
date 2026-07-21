package com.pabloverdejo.stockpilot.stock.dto;

import com.pabloverdejo.stockpilot.stock.MovementType;
import com.pabloverdejo.stockpilot.stock.StockMovement;

import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        String sku,
        MovementType type,
        int quantity,
        int resultingStock,
        String reason,
        String reference,
        Instant createdAt) {

    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getType(),
                movement.getQuantity(),
                movement.getResultingStock(),
                movement.getReason(),
                movement.getReference(),
                movement.getCreatedAt());
    }
}
