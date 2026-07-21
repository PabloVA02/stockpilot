package com.pabloverdejo.stockpilot.stock.dto;

import com.pabloverdejo.stockpilot.stock.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
        @NotNull MovementType type,
        @Min(1) int quantity,
        @NotBlank @Size(max = 200) String reason,
        @Size(max = 80) String reference) {
}
