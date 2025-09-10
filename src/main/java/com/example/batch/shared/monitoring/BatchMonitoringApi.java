package com.example.batch.shared.monitoring;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchMonitoringApi {

    private final JobExplorer jobExplorer;

    public BatchMonitoringApi(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    @GetMapping("/jobs")
    public Map<String, Object> getAllJobsStatus() {
        List<String> jobNames = jobExplorer.getJobNames();

        return Map.of(
                "totalJobs", jobNames.size(),
                "jobNames", jobNames,
                "jobStatuses", jobNames.stream()
                        .collect(Collectors.toMap(
                                jobName -> jobName,
                                this::getLatestJobStatus)));
    }

    @GetMapping("/jobs/{jobName}")
    public Map<String, Object> getJobStatus(@PathVariable String jobName) {
        try {
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 5);

            if (jobInstances.isEmpty()) {
                return Map.of("error", "No job instances found for: " + jobName);
            }

            return Map.of(
                    "jobName", jobName,
                    "totalInstances", jobExplorer.getJobInstanceCount(jobName),
                    "recentExecutions", jobInstances.stream()
                            .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                            .limit(10)
                            .map(this::mapJobExecution)
                            .toList());
        } catch (Exception e) {
            return Map.of("error", "Error fetching job details: " + e.getMessage());
        }
    }

    @GetMapping("/executions/{executionId}")
    public Map<String, Object> getExecutionDetails(@PathVariable Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            return Map.of("error", "Job execution not found");
        }

        return Map.of(
                "execution", mapJobExecution(execution),
                "steps", execution.getStepExecutions().stream()
                        .map(step -> Map.of(
                                "stepName", step.getStepName(),
                                "status", step.getStatus().toString(),
                                "readCount", step.getReadCount(),
                                "writeCount", step.getWriteCount(),
                                "skipCount", step.getSkipCount(),
                                "commitCount", step.getCommitCount(),
                                "rollbackCount", step.getRollbackCount(),
                                "startTime", step.getStartTime(),
                                "endTime", step.getEndTime()))
                        .toList());
    }

    private Map<String, Object> getLatestJobStatus(String jobName) {
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
        if (instances.isEmpty()) {
            return Map.of("status", "NO_EXECUTIONS", "message", "No job instances found");
        }

        JobInstance latestInstance = instances.get(0);
        List<JobExecution> executions = jobExplorer.getJobExecutions(latestInstance);
        if (executions.isEmpty()) {
            return Map.of("status", "NO_EXECUTIONS", "message", "No executions found");
        }

        JobExecution latestExecution = executions.get(executions.size() - 1);

        return Map.of(
                "status", latestExecution.getStatus().toString(),
                "exitCode", latestExecution.getExitStatus().getExitCode(),
                "startTime", latestExecution.getStartTime(),
                "endTime", latestExecution.getEndTime(),
                "jobParameters", latestExecution.getJobParameters().getParameters(),
                "stepSummary", latestExecution.getStepExecutions().stream()
                        .map(step -> Map.of(
                                "stepName", step.getStepName(),
                                "status", step.getStatus().toString(),
                                "readCount", step.getReadCount(),
                                "writeCount", step.getWriteCount(),
                                "skipCount", step.getSkipCount()))
                        .toList());
    }

    private Map<String, Object> mapJobExecution(JobExecution execution) {
        LocalDateTime startTime = execution.getStartTime();
        LocalDateTime endTime = execution.getEndTime();

        Duration duration = null;
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime);
        }

        return Map.of(
                "executionId", execution.getId(),
                "status", execution.getStatus().toString(),
                "exitCode", execution.getExitStatus().getExitCode(),
                "startTime", startTime,
                "endTime", endTime,
                "durationMs", duration != null ? duration.toMillis() : null,
                "stepCount", execution.getStepExecutions().size());
    }
}
