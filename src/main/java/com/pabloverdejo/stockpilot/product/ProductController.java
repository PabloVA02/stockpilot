package com.pabloverdejo.stockpilot.product;

import com.pabloverdejo.stockpilot.common.PageResponse;
import com.pabloverdejo.stockpilot.product.dto.ProductRequest;
import com.pabloverdejo.stockpilot.product.dto.ProductResponse;
import com.pabloverdejo.stockpilot.product.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List active products")
    PageResponse<ProductResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name")
            @Pattern(regexp = "name|sku|currentStock|unitPrice|createdAt") String sort,
            @RequestParam(defaultValue = "asc")
            @Pattern(regexp = "(?i)asc|desc") String direction) {
        return PageResponse.from(service.list(search, pageRequest(page, size, sort, direction)));
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable UUID id) {
        service.deactivate(id);
    }

    private Pageable pageRequest(int page, int size, String property, String direction) {
        var sortDirection = Sort.Direction.fromString(direction);
        var ordering = Sort.by(
                new Sort.Order(sortDirection, property),
                new Sort.Order(sortDirection, "id"));
        return PageRequest.of(page, size, ordering);
    }
}
