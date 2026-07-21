package com.pabloverdejo.stockpilot.product;

import com.pabloverdejo.stockpilot.common.BusinessRuleException;
import com.pabloverdejo.stockpilot.product.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository);
    }

    @Test
    void createsAProductWithNormalizedSku() {
        var request = new ProductRequest(
                " laptop-14 ", "Laptop 14", "Portable workstation", new BigDecimal("1299.90"), 10, 4);
        when(repository.existsBySkuIgnoreCase("LAPTOP-14")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request);

        assertThat(response.sku()).isEqualTo("LAPTOP-14");
        assertThat(response.currentStock()).isEqualTo(10);
        assertThat(response.lowStock()).isFalse();
        verify(repository).save(any(Product.class));
    }

    @Test
    void rejectsDuplicatedSku() {
        var request = new ProductRequest(
                "LAPTOP-14", "Laptop 14", null, new BigDecimal("1299.90"), 0, 4);
        when(repository.existsBySkuIgnoreCase("LAPTOP-14")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }
}
