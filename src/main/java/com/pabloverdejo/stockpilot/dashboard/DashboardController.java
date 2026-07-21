package com.pabloverdejo.stockpilot.dashboard;

import com.pabloverdejo.stockpilot.product.ProductRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
@SecurityRequirement(name = "basicAuth")
public class DashboardController {

    private final ProductRepository repository;

    public DashboardController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/summary")
    @Transactional(readOnly = true)
    DashboardSummary summary() {
        return new DashboardSummary(
                repository.countByActiveTrue(),
                repository.countLowStockProducts(),
                repository.sumAvailableUnits());
    }
}
