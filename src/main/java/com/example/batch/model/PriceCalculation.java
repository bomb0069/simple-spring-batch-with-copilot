package com.example.batch.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_calculations")
public class PriceCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "vat_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal vatAmount;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public PriceCalculation() {
        this.createdAt = LocalDateTime.now();
    }

    public PriceCalculation(BigDecimal originalPrice, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal totalPrice) {
        this();
        this.originalPrice = originalPrice;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PriceCalculation{" +
                "id=" + id +
                ", originalPrice=" + originalPrice +
                ", vatRate=" + vatRate +
                ", vatAmount=" + vatAmount +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                '}';
    }
}
