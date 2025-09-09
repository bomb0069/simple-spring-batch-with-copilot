package com.example.batch.model;

import java.math.BigDecimal;

public class PriceInput {
    private BigDecimal price;
    private BigDecimal vatRate;

    public PriceInput() {
    }

    public PriceInput(BigDecimal price, BigDecimal vatRate) {
        this.price = price;
        this.vatRate = vatRate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    @Override
    public String toString() {
        return "PriceInput{" +
                "price=" + price +
                ", vatRate=" + vatRate +
                '}';
    }
}
