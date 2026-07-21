package com.pabloverdejo.stockpilot.stock;

import com.pabloverdejo.stockpilot.common.PageResponse;
import com.pabloverdejo.stockpilot.stock.dto.StockMovementRequest;
import com.pabloverdejo.stockpilot.stock.dto.StockMovementResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/movements")
@Tag(name = "Stock movements")
@SecurityRequirement(name = "bearerAuth")
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
    PageResponse<StockMovementResponse> list(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt")
            @Pattern(regexp = "createdAt|quantity|resultingStock|type") String sort,
            @RequestParam(defaultValue = "desc")
            @Pattern(regexp = "(?i)asc|desc") String direction) {
        return PageResponse.from(service.list(productId, pageRequest(page, size, sort, direction)));
    }

    private Pageable pageRequest(int page, int size, String property, String direction) {
        var sortDirection = Sort.Direction.fromString(direction);
        var ordering = Sort.by(
                new Sort.Order(sortDirection, property),
                new Sort.Order(sortDirection, "id"));
        return PageRequest.of(page, size, ordering);
    }
}
