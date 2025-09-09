package com.example.batch.writer;

import com.example.batch.model.VatCalculationExport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom JSON Writer สำหรับเขียนข้อมูล VAT calculations เป็น JSON file
 * เพื่อส่งให้ระบบรอบข้าง
 */
@Component
public class JsonFileWriter implements ItemWriter<VatCalculationExport> {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileWriter.class);
    private final ObjectMapper objectMapper;
    private final String outputDirectory = "/app/data/exports";

    public JsonFileWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void write(Chunk<? extends VatCalculationExport> chunk) throws Exception {
        List<? extends VatCalculationExport> items = chunk.getItems();

        if (items.isEmpty()) {
            logger.info("No items to write to JSON file");
            return;
        }

        logger.info("Writing {} VAT calculation records to JSON file", items.size());

        // สร้าง output directory ถ้ายังไม่มี
        createOutputDirectory();

        // สร้างชื่อไฟล์ด้วย timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("vat_calculations_export_%s.json", timestamp);
        String filePath = outputDirectory + "/" + fileName;

        // สร้าง JSON structure พร้อม metadata
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("exportInfo", createExportMetadata(items.size()));
        exportData.put("vatCalculations", items);

        try {
            writeJsonToFile(exportData, filePath);
            logger.info("Successfully exported {} records to file: {}", items.size(), fileName);
        } catch (IOException e) {
            logger.error("Failed to write JSON file: {}", filePath, e);
            throw new RuntimeException("Failed to export data to JSON file", e);
        }
    }

    private void createOutputDirectory() {
        File directory = new File(outputDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.info("Created output directory: {}", outputDirectory);
            } else {
                logger.warn("Failed to create output directory: {}", outputDirectory);
            }
        }
    }

    private Map<String, Object> createExportMetadata(int recordCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportTimestamp", LocalDateTime.now());
        metadata.put("recordCount", recordCount);
        metadata.put("source", "batch-processing-system");
        metadata.put("version", "1.0");
        metadata.put("format", "JSON");
        return metadata;
    }

    private void writeJsonToFile(Map<String, Object> data, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            objectMapper.writeValue(fileWriter, data);
        }
    }
}
