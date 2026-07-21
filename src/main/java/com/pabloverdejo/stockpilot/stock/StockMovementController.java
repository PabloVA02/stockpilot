package com.pabloverdejo.stockpilot.stock;

import com.pabloverdejo.stockpilot.stock.dto.StockMovementRequest;
import com.pabloverdejo.stockpilot.stock.dto.StockMovementResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/movements")
@Tag(name = "Stock movements")
@SecurityRequirement(name = "basicAuth")
public class StockMovementController {

    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StockMovementResponse register(
            @PathVariable UUID productId,
            @Valid @RequestBody StockMovementRequest request) {
        return service.register(productId, request);
    }

    @GetMapping
    Page<StockMovementResponse> list(@PathVariable UUID productId, Pageable pageable) {
        return service.list(productId, pageable);
    }
}
