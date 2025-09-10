package com.example.batch.shared.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Job execution listener that records custom metrics for batch jobs
 */
public class BatchJobMetricsListener implements JobExecutionListener {

    // Constants for tag names
    private static final String JOB_NAME_TAG = "job_name";
    private static final String STEP_NAME_TAG = "step_name";
    private static final String STATUS_TAG = "status";

    private final MeterRegistry meterRegistry;

    public BatchJobMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Record job start
        meterRegistry.counter("batch.job.started",
                JOB_NAME_TAG, jobExecution.getJobInstance().getJobName())
                .increment();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().toString();

        // Record job completion
        meterRegistry.counter("batch.job.completed",
                JOB_NAME_TAG, jobName,
                STATUS_TAG, status)
                .increment();

        // Record job duration
        if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
            LocalDateTime startTime = jobExecution.getStartTime();
            LocalDateTime endTime = jobExecution.getEndTime();
            Duration duration = Duration.between(startTime, endTime);

            meterRegistry.timer("batch.job.duration",
                    JOB_NAME_TAG, jobName,
                    STATUS_TAG, status)
                    .record(duration);
        }

        // Record step metrics
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            String stepName = stepExecution.getStepName();
            Tags tags = Tags.of(JOB_NAME_TAG, jobName, STEP_NAME_TAG, stepName);

            meterRegistry.gauge("batch.step.read_count", tags, stepExecution.getReadCount());
            meterRegistry.gauge("batch.step.write_count", tags, stepExecution.getWriteCount());
            meterRegistry.gauge("batch.step.skip_count", tags, stepExecution.getSkipCount());
            meterRegistry.gauge("batch.step.filter_count", tags, stepExecution.getFilterCount());
        }
    }
}
