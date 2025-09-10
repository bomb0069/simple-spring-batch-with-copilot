package com.example.batch.shared.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom Spring Batch metrics with Micrometer and Prometheus
 */
@Configuration
public class MetricsConfig {

    @Bean
    public BatchJobMetricsListener batchJobMetricsListener(MeterRegistry meterRegistry) {
        return new BatchJobMetricsListener(meterRegistry);
    }

    @Bean
    public BatchStepMetricsListener batchStepMetricsListener(MeterRegistry meterRegistry) {
        return new BatchStepMetricsListener(meterRegistry);
    }
}
