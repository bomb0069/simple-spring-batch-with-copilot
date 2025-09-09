package com.example.batch.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class BatchJobRunnerTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    void testBatchJobExecution() throws Exception {
        // Create job parameters
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // Launch the job
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Assert job completed successfully (even if no data found)
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}
