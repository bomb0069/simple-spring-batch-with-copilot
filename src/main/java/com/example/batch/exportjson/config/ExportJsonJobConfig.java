package com.example.batch.exportjson.config;

import com.example.batch.vatcalculation.model.PriceCalculation;
import com.example.batch.exportjson.model.VatCalculationExport;
import com.example.batch.exportjson.processor.ExportTransformProcessor;
import com.example.batch.exportjson.writer.JsonFileWriter;
import com.example.batch.shared.repository.PriceCalculationRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class ExportJsonJobConfig {

    private final JobRepository jobRepository;

    public ExportJsonJobConfig(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Reader สำหรับอ่านข้อมูลจาก price_calculations table
    @Bean
    public RepositoryItemReader<PriceCalculation> exportReader(PriceCalculationRepository repository) {
        return new RepositoryItemReaderBuilder<PriceCalculation>()
                .name("priceCalculationReader")
                .repository(repository)
                .methodName("findAll")
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(10)
                .build();
    }

    // Step สำหรับ Export JSON: Read -> Transform -> Write
    @Bean
    public Step exportToJsonStep(
            @Qualifier("businessTransactionManager") PlatformTransactionManager transactionManager,
            RepositoryItemReader<PriceCalculation> exportReader,
            ExportTransformProcessor exportTransformProcessor,
            JsonFileWriter jsonFileWriter) {
        return new StepBuilder("exportToJsonStep", jobRepository)
                .<PriceCalculation, VatCalculationExport>chunk(10, transactionManager)
                .reader(exportReader)
                .processor(exportTransformProcessor)
                .writer(jsonFileWriter)
                .build();
    }

    // Job สำหรับ Export JSON
    @Bean
    public Job exportVatCalculationsJob(Step exportToJsonStep) {
        return new JobBuilder("exportVatCalculationsJob", jobRepository)
                .start(exportToJsonStep)
                .build();
    }
}
