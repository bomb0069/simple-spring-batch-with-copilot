package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BatchJobRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobRunner.class);

    private final JobLauncher jobLauncher;
    private final Job vatCalculationJob;

    @Value("${batch.auto-run.enabled:false}")
    private boolean autoRunEnabled;

    public BatchJobRunner(JobLauncher jobLauncher, Job vatCalculationJob) {
        this.jobLauncher = jobLauncher;
        this.vatCalculationJob = vatCalculationJob;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!autoRunEnabled) {
            logger.info("🛑 Batch auto-run is disabled. Use REST API endpoints to trigger jobs manually.");
            logger.info("   - POST /api/batch/run/vat-calculation");
            logger.info("   - POST /api/batch/run/export-json");
            logger.info("   - GET  /api/batch/jobs (monitoring)");
            return;
        }

        logger.info("🚀 Starting VAT Calculation Batch Job...");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            var jobExecution = jobLauncher.run(vatCalculationJob, jobParameters);

            logger.info("✅ Batch Job completed with status: {}", jobExecution.getStatus());
            logger.info("📊 Job Execution Summary:");
            logger.info("   - Job Instance ID: {}", jobExecution.getJobInstance().getId());
            logger.info("   - Start Time: {}", jobExecution.getStartTime());
            logger.info("   - End Time: {}", jobExecution.getEndTime());
            logger.info("   - Status: {}", jobExecution.getStatus());

            jobExecution.getStepExecutions()
                    .forEach(stepExecution -> logger.info("   - Step '{}': Read={}, Written={}, Skipped={}",
                            stepExecution.getStepName(),
                            stepExecution.getReadCount(),
                            stepExecution.getWriteCount(),
                            stepExecution.getSkipCount()));

        } catch (Exception e) {
            logger.error("❌ Batch Job failed with error: {}", e.getMessage());
            logger.error("   Exception details: ", e);
        }
    }
}
