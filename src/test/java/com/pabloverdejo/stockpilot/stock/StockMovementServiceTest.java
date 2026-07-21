package com.pabloverdejo.stockpilot.stock;

import com.pabloverdejo.stockpilot.common.BusinessRuleException;
import com.pabloverdejo.stockpilot.product.Product;
import com.pabloverdejo.stockpilot.product.ProductRepository;
import com.pabloverdejo.stockpilot.stock.dto.StockMovementRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementRepository movementRepository;

    private StockMovementService service;

    @BeforeEach
    void setUp() {
        service = new StockMovementService(productRepository, movementRepository);
    }

    @Test
    void outboundMovementUpdatesStockAtomically() {
        var productId = UUID.randomUUID();
        var product = new Product("SKU-1", "Keyboard", null, new BigDecimal("79.90"), 12, 3);
        when(productRepository.findActiveByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.register(
                productId,
                new StockMovementRequest(MovementType.OUTBOUND, 5, "Customer order", "ORDER-42"));

        assertThat(response.resultingStock()).isEqualTo(7);
        assertThat(product.getCurrentStock()).isEqualTo(7);
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void rejectsOutboundMovementWithoutEnoughStock() {
        var productId = UUID.randomUUID();
        var product = new Product("SKU-1", "Keyboard", null, new BigDecimal("79.90"), 2, 3);
        when(productRepository.findActiveByIdForUpdate(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.register(
                productId,
                new StockMovementRequest(MovementType.OUTBOUND, 5, "Customer order", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");

        assertThat(product.getCurrentStock()).isEqualTo(2);
        verify(movementRepository, never()).save(any());
    }
}
