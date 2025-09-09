package com.example.batch.config;

import com.example.batch.model.PriceInput;
import com.example.batch.model.PriceCalculation;
import com.example.batch.model.VatCalculationExport;
import com.example.batch.processor.VatCalculationProcessor;
import com.example.batch.processor.ExportTransformProcessor;
import com.example.batch.writer.JsonFileWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import com.example.batch.repository.PriceCalculationRepository;

import java.util.Map;

@Configuration
public class BatchConfiguration {

    private final JobRepository jobRepository;

    public BatchConfiguration(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Step 1: Reader - อ่านข้อมูลจาก CSV file
    @Bean
    public FlatFileItemReader<PriceInput> reader() {
        BeanWrapperFieldSetMapper<PriceInput> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(PriceInput.class);

        return new FlatFileItemReaderBuilder<PriceInput>()
                .name("priceItemReader")
                .resource(new ClassPathResource("input-data.csv"))
                .delimited()
                .names("price", "vatRate")
                .linesToSkip(1) // Skip header line
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    // Step 3: Writer - บันทึกข้อมูลลง Database
    @Bean
    public RepositoryItemWriter<PriceCalculation> writer(PriceCalculationRepository priceCalculationRepository) {
        RepositoryItemWriter<PriceCalculation> writer = new RepositoryItemWriter<>();
        writer.setRepository(priceCalculationRepository);
        writer.setMethodName("save");
        return writer;
    }

    // สร้าง Step ที่รวม 3 ขั้นตอน: Read -> Process -> Write
    @Bean
    public Step processVatCalculationStep(
            @Qualifier("businessTransactionManager") PlatformTransactionManager transactionManager,
            VatCalculationProcessor vatCalculationProcessor,
            RepositoryItemWriter<PriceCalculation> writer) {
        return new StepBuilder("processVatCalculationStep", jobRepository)
                .<PriceInput, PriceCalculation>chunk(10, transactionManager)
                .reader(reader())
                .processor(vatCalculationProcessor) // Step 2: Processor - คำนวณ VAT
                .writer(writer)
                .build();
    }

    // สร้าง Job ที่ประกอบด้วย Step
    @Bean
    public Job vatCalculationJob(Step processVatCalculationStep) {
        return new JobBuilder("vatCalculationJob", jobRepository)
                .start(processVatCalculationStep)
                .build();
    }

    // ======= JSON Export Job Configuration =======

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
