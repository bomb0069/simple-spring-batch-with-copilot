package com.example.batch.vatcalculation.config;

import com.example.batch.vatcalculation.model.PriceInput;
import com.example.batch.vatcalculation.model.PriceCalculation;
import com.example.batch.vatcalculation.processor.VatCalculationProcessor;
import com.example.batch.shared.repository.PriceCalculationRepository;
import com.example.batch.shared.config.BatchJobMetricsListener;
import com.example.batch.shared.config.BatchStepMetricsListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class VatCalculationJobConfig {

    private final JobRepository jobRepository;

    public VatCalculationJobConfig(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Step 1: Reader - อ่านข้อมูลจาก CSV file
    @Bean
    public FlatFileItemReader<PriceInput> vatCalculationReader() {
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
    public RepositoryItemWriter<PriceCalculation> vatCalculationWriter(
            PriceCalculationRepository priceCalculationRepository) {
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
            RepositoryItemWriter<PriceCalculation> vatCalculationWriter,
            @Qualifier("batchStepMetricsListener") BatchStepMetricsListener stepMetricsListener) {
        return new StepBuilder("processVatCalculationStep", jobRepository)
                .<PriceInput, PriceCalculation>chunk(10, transactionManager)
                .reader(vatCalculationReader())
                .processor(vatCalculationProcessor) // Step 2: Processor - คำนวณ VAT
                .writer(vatCalculationWriter)
                .listener(stepMetricsListener)
                .build();
    }

    // สร้าง Job ที่ประกอบด้วย Step
    @Bean
    public Job vatCalculationJob(Step processVatCalculationStep,
            @Qualifier("batchJobMetricsListener") BatchJobMetricsListener jobMetricsListener) {
        return new JobBuilder("vatCalculationJob", jobRepository)
                .start(processVatCalculationStep)
                .listener(jobMetricsListener)
                .build();
    }
}
