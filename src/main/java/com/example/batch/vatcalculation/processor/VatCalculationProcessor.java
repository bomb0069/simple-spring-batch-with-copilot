package com.example.batch.vatcalculation.processor;

import com.example.batch.vatcalculation.model.PriceInput;
import com.example.batch.vatcalculation.model.PriceCalculation;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class VatCalculationProcessor implements ItemProcessor<PriceInput, PriceCalculation> {

        private static final Logger logger = LoggerFactory.getLogger(VatCalculationProcessor.class);

        @Override
        public PriceCalculation process(PriceInput priceInput) throws Exception {
                logger.info("Processing price: {} with VAT rate: {}", priceInput.getPrice(), priceInput.getVatRate());

                Thread.sleep(3000);

                // คำนวณ VAT Amount = Price * VAT Rate
                BigDecimal vatAmount = priceInput.getPrice()
                                .multiply(priceInput.getVatRate())
                                .setScale(2, RoundingMode.HALF_UP);

                // คำนวณ Total Price = Price + VAT Amount
                BigDecimal totalPrice = priceInput.getPrice()
                                .add(vatAmount)
                                .setScale(2, RoundingMode.HALF_UP);

                PriceCalculation calculation = new PriceCalculation(
                                priceInput.getPrice(),
                                priceInput.getVatRate(),
                                vatAmount,
                                totalPrice);

                logger.info("Calculated VAT: {} Total: {}", vatAmount, totalPrice);
                return calculation;
        }
}
