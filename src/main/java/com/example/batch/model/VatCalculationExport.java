package com.example.batch.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO สำหรับ export ข้อมูล VAT calculation เป็น JSON
 * เพื่อส่งให้ระบบรอบข้าง
 */
public class VatCalculationExport {

    private Long id;
    private BigDecimal originalPrice;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal totalPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    // Default constructor
    public VatCalculationExport() {
    }

    // Constructor for conversion from PriceCalculation
    public VatCalculationExport(PriceCalculation priceCalculation) {
        this.id = priceCalculation.getId();
        this.originalPrice = priceCalculation.getOriginalPrice();
        this.vatRate = priceCalculation.getVatRate();
        this.vatAmount = priceCalculation.getVatAmount();
        this.totalPrice = priceCalculation.getTotalPrice();
        this.processedAt = priceCalculation.getCreatedAt();
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

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "VatCalculationExport{" +
                "id=" + id +
                ", originalPrice=" + originalPrice +
                ", vatRate=" + vatRate +
                ", vatAmount=" + vatAmount +
                ", totalPrice=" + totalPrice +
                ", processedAt=" + processedAt +
                '}';
    }
}
