# Prometheus Metrics Configuration Documentation

## Available Endpoints

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Custom Spring Batch Metrics

### Job Metrics

- `batch_job_started_total` - Counter of job starts by job_name
- `batch_job_completed_total` - Counter of job completions by job_name and status
- `batch_job_duration_seconds` - Timer for job execution duration by job_name and status

### Step Metrics

- `batch_step_started_total` - Counter of step starts by job_name and step_name
- `batch_step_completed_total` - Counter of step completions by job_name, step_name, and status
- `batch_step_duration_seconds` - Timer for step execution duration by job_name, step_name, and status
- `batch_step_read_count` - Gauge for number of items read by job_name and step_name
- `batch_step_write_count` - Gauge for number of items written by job_name and step_name
- `batch_step_skip_count` - Gauge for number of items skipped by job_name and step_name
- `batch_step_filter_count` - Gauge for number of items filtered by job_name and step_name

## Standard Spring Boot Metrics

- JVM metrics (memory, GC, threads)
- HTTP request metrics
- Database connection pool metrics
- System metrics (CPU, disk, etc.)

## Sample Prometheus Configuration

```yaml
scrape_configs:
  - job_name: "spring-batch-app"
    static_configs:
      - targets: ["localhost:8080"]
    metrics_path: "/actuator/prometheus"
    scrape_interval: 15s
```

## Sample Grafana Queries

### Job Success Rate

```
rate(batch_job_completed_total{status="COMPLETED"}[5m]) / rate(batch_job_completed_total[5m])
```

### Average Job Duration

```
rate(batch_job_duration_seconds_sum[5m]) / rate(batch_job_duration_seconds_count[5m])
```

### Items Processed Per Second

```
rate(batch_step_write_count[5m])
```
