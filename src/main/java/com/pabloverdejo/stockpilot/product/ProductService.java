package com.pabloverdejo.stockpilot.product;

import com.pabloverdejo.stockpilot.common.BusinessRuleException;
import com.pabloverdejo.stockpilot.common.ResourceNotFoundException;
import com.pabloverdejo.stockpilot.product.dto.ProductRequest;
import com.pabloverdejo.stockpilot.product.dto.ProductResponse;
import com.pabloverdejo.stockpilot.product.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String search, Pageable pageable) {
        return repository.findByActiveTrueAndNameContainingIgnoreCase(search == null ? "" : search, pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        return ProductResponse.from(findActive(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        var normalizedSku = request.sku().trim().toUpperCase(Locale.ROOT);
        if (repository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new BusinessRuleException("A product with SKU '" + normalizedSku + "' already exists.");
        }
        var product = new Product(
                normalizedSku,
                request.name().trim(),
                normalize(request.description()),
                request.unitPrice(),
                request.initialStock(),
                request.reorderLevel());
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductUpdateRequest request) {
        var product = findActive(id);
        product.updateDetails(
                request.name().trim(),
                normalize(request.description()),
                request.unitPrice(),
                request.reorderLevel());
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(UUID id) {
        findActive(id).deactivate();
    }

    private Product findActive(UUID id) {
        return repository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product '" + id + "' does not exist."));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
