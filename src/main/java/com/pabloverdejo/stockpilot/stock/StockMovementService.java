package com.pabloverdejo.stockpilot.stock;

import com.pabloverdejo.stockpilot.common.BusinessRuleException;
import com.pabloverdejo.stockpilot.common.ResourceNotFoundException;
import com.pabloverdejo.stockpilot.product.ProductRepository;
import com.pabloverdejo.stockpilot.stock.dto.StockMovementRequest;
import com.pabloverdejo.stockpilot.stock.dto.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StockMovementService {

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;

    public StockMovementService(ProductRepository productRepository, StockMovementRepository movementRepository) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public StockMovementResponse register(UUID productId, StockMovementRequest request) {
        var product = productRepository.findActiveByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product '" + productId + "' does not exist."));

        if (request.type() == MovementType.OUTBOUND && product.getCurrentStock() < request.quantity()) {
            throw new BusinessRuleException(
                    "Insufficient stock. Available: " + product.getCurrentStock() + ", requested: " + request.quantity());
        }

        if (request.type() == MovementType.INBOUND) {
            product.addStock(request.quantity());
        } else {
            product.removeStock(request.quantity());
        }

        var movement = new StockMovement(
                product,
                request.type(),
                request.quantity(),
                product.getCurrentStock(),
                request.reason().trim(),
                normalize(request.reference()));
        return StockMovementResponse.from(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> list(UUID productId, Pageable pageable) {
        return movementRepository.findByProductId(productId, pageable).map(StockMovementResponse::from);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
