package com.example.batch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller สำหรับจัดการการเรียกใช้ Batch Jobs
 */
@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job vatCalculationJob;
    private final Job exportVatCalculationsJob;

    public BatchJobController(JobLauncher jobLauncher,
            @Qualifier("vatCalculationJob") Job vatCalculationJob,
            @Qualifier("exportVatCalculationsJob") Job exportVatCalculationsJob) {
        this.jobLauncher = jobLauncher;
        this.vatCalculationJob = vatCalculationJob;
        this.exportVatCalculationsJob = exportVatCalculationsJob;
    }

    /**
     * เรียกใช้ VAT Calculation Job (อ่าน CSV -> คำนวณ VAT -> บันทึก DB)
     */
    @PostMapping("/run/vat-calculation")
    public ResponseEntity<Map<String, Object>> runVatCalculationJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            var jobExecution = jobLauncher.run(vatCalculationJob, jobParameters);

            return ResponseEntity.ok(Map.of(
                    "message", "VAT Calculation Job started successfully",
                    "jobId", jobExecution.getId(),
                    "status", jobExecution.getStatus().toString(),
                    "startTime", jobExecution.getStartTime()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to start VAT Calculation Job",
                    "message", e.getMessage()));
        }
    }

    /**
     * เรียกใช้ Export Job (อ่าน DB -> แปลงข้อมูล -> สร้าง JSON)
     */
    @PostMapping("/run/export-json")
    public ResponseEntity<Map<String, Object>> runExportJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            var jobExecution = jobLauncher.run(exportVatCalculationsJob, jobParameters);

            return ResponseEntity.ok(Map.of(
                    "message", "Export JSON Job started successfully",
                    "jobId", jobExecution.getId(),
                    "status", jobExecution.getStatus().toString(),
                    "startTime", jobExecution.getStartTime(),
                    "outputLocation", "/app/data/exports/"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to start Export JSON Job",
                    "message", e.getMessage()));
        }
    }
}
