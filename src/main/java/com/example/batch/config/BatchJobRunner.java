package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

@Component
public class BatchJobRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobRunner.class);

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;

    @Value("${batch.auto-run.enabled:false}")
    private boolean autoRunEnabled;

    public BatchJobRunner(JobLauncher jobLauncher,
            @Qualifier("vatCalculationJob") Job vatCalculationJob,
            @Qualifier("exportVatCalculationsJob") Job exportVatCalculationsJob) {
        this.jobLauncher = jobLauncher;
        this.jobs = new HashMap<>();
        this.jobs.put("vat-calculation", vatCalculationJob);
        this.jobs.put("export-json", exportVatCalculationsJob);
        this.jobs.put("vatCalculationJob", vatCalculationJob);
        this.jobs.put("exportVatCalculationsJob", exportVatCalculationsJob);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("📋 Available command line arguments: {}", Arrays.toString(args));

        // Check if specific job is requested via command line
        String requestedJob = findJobArgument(args);

        if (requestedJob != null) {
            logger.info("🎯 Running specific job from command line: {}", requestedJob);
            executeJob(requestedJob);
            return;
        }

        // Check auto-run configuration
        if (!autoRunEnabled) {
            logger.info(
                    "🛑 Batch auto-run is disabled. Use command line arguments or REST API endpoints to trigger jobs manually.");
            logger.info("💡 Command Line Usage:");
            logger.info("   java -jar app.jar --job=vat-calculation");
            logger.info("   java -jar app.jar --job=export-json");
            logger.info("   java -jar app.jar --job=vatCalculationJob");
            logger.info("   java -jar app.jar --job=exportVatCalculationsJob");
            logger.info("🌐 REST API Endpoints:");
            logger.info("   - POST /api/batch/run/vat-calculation");
            logger.info("   - POST /api/batch/run/export-json");
            logger.info("   - GET  /api/batch/jobs (monitoring)");
            return;
        }

        // Default behavior when auto-run is enabled (run VAT calculation job)
        logger.info("🚀 Auto-run enabled: Starting default VAT Calculation Batch Job...");
        executeJob("vat-calculation");
    }

    private String findJobArgument(String... args) {
        for (String arg : args) {
            if (arg.startsWith("--job=")) {
                return arg.substring(6); // Remove "--job=" prefix
            }
        }
        return null;
    }

    private void executeJob(String jobName) throws Exception {
        Job job = jobs.get(jobName);
        if (job == null) {
            logger.error("❌ Job '{}' not found. Available jobs: {}", jobName, jobs.keySet());
            return;
        }

        logger.info("🚀 Starting job: {} ({})", jobName, job.getName());

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .addString("jobName", jobName)
                    .toJobParameters();

            var jobExecution = jobLauncher.run(job, jobParameters);

            logger.info("✅ Batch Job '{}' completed with status: {}", jobName, jobExecution.getStatus());
            logger.info("📊 Job Execution Summary:");
            logger.info("   - Job Name: {}", job.getName());
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
            logger.error("❌ Batch Job '{}' failed with error: {}", jobName, e.getMessage());
            logger.error("   Exception details: ", e);
            throw e;
        }
    }
}
