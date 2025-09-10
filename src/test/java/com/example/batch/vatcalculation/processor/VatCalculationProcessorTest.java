package com.example.batch.vatcalculation.processor;

import com.example.batch.vatcalculation.model.PriceInput;
import com.example.batch.vatcalculation.model.PriceCalculation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VatCalculationProcessorTest {

    private VatCalculationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new VatCalculationProcessor();
    }

    @Test
    void testVatCalculation() throws Exception {
        // Given
        PriceInput input = new PriceInput();
        input.setPrice(new BigDecimal("100.00"));
        input.setVatRate(new BigDecimal("0.07"));

        // When
        PriceCalculation result = processor.process(input);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getOriginalPrice());
        assertEquals(new BigDecimal("0.07"), result.getVatRate());
        assertEquals(new BigDecimal("7.00"), result.getVatAmount());
        assertEquals(new BigDecimal("107.00"), result.getTotalPrice());
    }

    @Test
    void testVatCalculationWithDifferentRate() throws Exception {
        // Given
        PriceInput input = new PriceInput();
        input.setPrice(new BigDecimal("250.50"));
        input.setVatRate(new BigDecimal("0.10"));

        // When
        PriceCalculation result = processor.process(input);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("250.50"), result.getOriginalPrice());
        assertEquals(new BigDecimal("0.10"), result.getVatRate());
        assertEquals(new BigDecimal("25.05"), result.getVatAmount());
        assertEquals(new BigDecimal("275.55"), result.getTotalPrice());
    }
}
