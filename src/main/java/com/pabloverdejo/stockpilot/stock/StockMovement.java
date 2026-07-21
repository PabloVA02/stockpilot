package com.pabloverdejo.stockpilot.stock;

import com.pabloverdejo.stockpilot.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int resultingStock;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(length = 80)
    private String reference;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    public StockMovement(Product product, MovementType type, int quantity, int resultingStock, String reason,
                         String reference) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.resultingStock = resultingStock;
        this.reason = reason;
        this.reference = reference;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public MovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getResultingStock() {
        return resultingStock;
    }

    public String getReason() {
        return reason;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
