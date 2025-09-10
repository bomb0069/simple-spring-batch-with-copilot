package com.example.batch.shared.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Step execution listener for detailed step metrics
 */
public class BatchStepMetricsListener implements StepExecutionListener {

    // Constants for tag names
    private static final String JOB_NAME_TAG = "job_name";
    private static final String STEP_NAME_TAG = "step_name";
    private static final String STATUS_TAG = "status";

    private final MeterRegistry meterRegistry;
    private Timer.Sample sample;

    public BatchStepMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();

        // Start timing the step
        sample = Timer.start(meterRegistry);

        // Record step start
        meterRegistry.counter("batch.step.started",
                JOB_NAME_TAG, jobName,
                STEP_NAME_TAG, stepName)
                .increment();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();
        String status = stepExecution.getStatus().toString();

        // Stop timing and record step duration
        if (sample != null) {
            sample.stop(Timer.builder("batch.step.duration")
                    .tag(JOB_NAME_TAG, jobName)
                    .tag(STEP_NAME_TAG, stepName)
                    .tag(STATUS_TAG, status)
                    .register(meterRegistry));
        }

        // Record step completion
        meterRegistry.counter("batch.step.completed",
                JOB_NAME_TAG, jobName,
                STEP_NAME_TAG, stepName,
                STATUS_TAG, status)
                .increment();

        return null;
    }
}
