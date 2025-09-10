package com.example.batch.exportjson.processor;

import com.example.batch.vatcalculation.model.PriceCalculation;
import com.example.batch.exportjson.model.VatCalculationExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Processor สำหรับแปลง PriceCalculation เป็น VatCalculationExport
 * เพื่อเตรียมข้อมูลสำหรับ export เป็น JSON
 */
@Component
public class ExportTransformProcessor implements ItemProcessor<PriceCalculation, VatCalculationExport> {

    private static final Logger logger = LoggerFactory.getLogger(ExportTransformProcessor.class);

    @Override
    public VatCalculationExport process(PriceCalculation priceCalculation) throws Exception {
        logger.debug("Processing PriceCalculation ID: {} for export", priceCalculation.getId());

        // แปลง PriceCalculation เป็น VatCalculationExport
        VatCalculationExport exportData = new VatCalculationExport(priceCalculation);

        logger.debug("Transformed PriceCalculation ID: {} to VatCalculationExport",
                priceCalculation.getId());

        return exportData;
    }
}
