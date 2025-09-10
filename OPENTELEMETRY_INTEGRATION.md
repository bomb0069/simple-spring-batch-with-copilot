# OpenTelemetry Integration with LGTM Stack

## 🔭 Overview

This document describes the **OpenTelemetry integration** with the **LGTM observability stack** (Loki, Grafana, Tempo, Mimir) for comprehensive monitoring and observability of the Spring Batch application.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Batch App     │    │ OTel Collector  │    │   LGTM Stack    │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │  ┌───────────┐  │
│ │ OTel Agent  │─┼────┼▶│   Receiver  │ │    │  │ Grafana   │  │
│ └─────────────┘ │    │ └─────────────┘ │    │  └───────────┘  │
│                 │    │        │        │    │  ┌───────────┐  │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │  │   Tempo   │◀─┤
│ │Spring Batch │ │    │ │ Processors  │ │    │  └───────────┘  │
│ └─────────────┘ │    │ └─────────────┘ │    │  ┌───────────┐  │
│                 │    │        │        │    │  │   Loki    │◀─┤
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │  └───────────┘  │
│ │  Actuator   │ │    │ │  Exporters  │─┼────┤  ┌───────────┐  │
│ └─────────────┘ │    │ └─────────────┘ │    │  │   Mimir   │◀─┤
└─────────────────┘    └─────────────────┘    │  └───────────┘  │
                                              │  ┌───────────┐  │
                                              │  │Prometheus │  │
                                              │  └───────────┘  │
                                              └─────────────────┘
```

## 📦 Components

### 1. OpenTelemetry Java Agent

- **Location**: Downloaded in Dockerfile (`/app/opentelemetry-javaagent.jar`)
- **Version**: Latest from GitHub releases
- **Purpose**: Auto-instrumentation of Java applications
- **Configuration**: Via environment variables

### 2. LGTM Stack Components

#### Grafana (Port 3000)

- **Purpose**: Visualization and dashboards
- **Login**: admin/admin
- **Features**: Pre-configured datasources for all telemetry data

#### Loki (Port 3100)

- **Purpose**: Log aggregation and storage
- **Protocol**: HTTP API
- **Integration**: Receives logs from OTel Collector

#### Tempo (Port 3200)

- **Purpose**: Distributed tracing
- **Protocols**: OTLP gRPC (4317), OTLP HTTP (4318)
- **Features**: Trace correlation with logs and metrics

#### Mimir (Port 9009)

- **Purpose**: Long-term metrics storage
- **Protocol**: Prometheus remote write
- **Features**: High-availability metrics storage

#### Prometheus (Port 9090)

- **Purpose**: Metrics collection and querying
- **Targets**: Batch app, OTel Collector, other services
- **Integration**: Scrapes /actuator/prometheus endpoint

### 3. OpenTelemetry Collector

- **Purpose**: Telemetry data pipeline
- **Protocols**: OTLP gRPC/HTTP, Prometheus scraping
- **Processing**: Batching, resource attribution, routing

## 🔧 Configuration

### Environment Variables (Docker)

```bash
# Service identification
OTEL_SERVICE_NAME=batch-processing
OTEL_SERVICE_VERSION=1.0.0
OTEL_RESOURCE_ATTRIBUTES=service.name=batch-processing,service.version=1.0.0,deployment.environment=docker

# Exporter configuration
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_LOGS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp

# Instrumentation enablement
OTEL_INSTRUMENTATION_SPRING_BATCH_ENABLED=true
OTEL_INSTRUMENTATION_JDBC_ENABLED=true
OTEL_INSTRUMENTATION_JPA_ENABLED=true
OTEL_INSTRUMENTATION_SPRING_WEB_ENABLED=true
OTEL_INSTRUMENTATION_SPRING_WEBMVC_ENABLED=true
OTEL_INSTRUMENTATION_HIKARICP_ENABLED=true
```

### OTel Collector Configuration

- **File**: `docker/otel/otel-collector-config.yaml`
- **Receivers**: OTLP, Prometheus scraping
- **Processors**: Batch processing, resource attribution
- **Exporters**: Tempo, Loki, Prometheus, Mimir

## 🚀 Usage

### Quick Start

```bash
# Start the full observability stack
./observability.sh start

# Check status
./observability.sh status

# View logs
./observability.sh logs

# Health check
./observability.sh health

# Stop everything
./observability.sh stop
```

### Manual Start

```bash
# Start observability stack first
docker-compose -f observation-compose.yaml up -d

# Start main application with observability
docker-compose up -d
```

### Alternative: Docker Compose Override

```bash
# Using override file (experimental)
docker-compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

## 📊 Available Telemetry Data

### 🔍 Traces (Tempo)

- **Spring Batch job execution traces**
- **Database query traces** (JDBC/JPA)
- **HTTP request traces** (REST API calls)
- **Custom business logic spans**

**Example trace data:**

- Job execution: `batch.job.execution`
- Step execution: `batch.step.execution`
- Database operations: `jdbc.query`, `hibernate.session`
- HTTP requests: `http.server.request`

### 📈 Metrics (Prometheus/Mimir)

- **Custom batch metrics** (from Prometheus integration)
- **JVM metrics** (memory, GC, threads)
- **Database connection pool metrics** (HikariCP)
- **HTTP server metrics** (request rates, latencies)
- **OpenTelemetry instrumentation metrics**

**Example metrics:**

```promql
# Custom batch metrics (from previous Prometheus integration)
batch_job_duration_seconds
batch_step_completed_total

# Auto-instrumented metrics
http_server_requests_seconds
jdbc_connections_active
jvm_memory_used_bytes
```

### 📋 Logs (Loki)

- **Application logs** (Spring Boot logging)
- **Batch job execution logs**
- **Database query logs** (if enabled)
- **Error and exception logs**
- **Trace correlation** (logs linked to traces)

**Log labels:**

- `service_name="batch-processing"`
- `level="INFO|WARN|ERROR"`
- `job_name="vatCalculationJob"`
- `trace_id="abc123..."` (for correlation)

## 🎛️ Dashboards and Visualization

### Grafana Dashboards

Access at: **http://localhost:3000** (admin/admin)

**Pre-configured data sources:**

- **Prometheus**: Default metrics source
- **Tempo**: Distributed tracing
- **Loki**: Log aggregation
- **Mimir**: Long-term metrics storage

**Recommended dashboard queries:**

#### Batch Job Monitoring

```promql
# Job execution rate
rate(batch_job_completed_total[5m])

# Job execution duration
histogram_quantile(0.95, rate(batch_job_duration_seconds_bucket[5m]))

# Failed jobs
increase(batch_job_completed_total{status!="COMPLETED"}[1h])
```

#### System Monitoring

```promql
# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# Database connections
hikaricp_connections_active

# HTTP request rate
rate(http_server_requests_seconds_count[5m])
```

### Trace Analysis (Tempo)

- **End-to-end job execution traces**
- **Database performance analysis**
- **Error correlation across services**
- **Performance bottleneck identification**

### Log Analysis (Loki)

```logql
# Application logs for specific job
{service_name="batch-processing"} |= "vatCalculationJob"

# Error logs with trace correlation
{service_name="batch-processing", level="ERROR"} | json | trace_id != ""

# Performance logs
{service_name="batch-processing"} |= "completed in" | regexp "completed in (?P<duration>\\d+)ms"
```

## 🔧 Troubleshooting

### Common Issues

#### 1. OTel Agent Not Working

```bash
# Check if agent is loaded
docker logs batch-processing-app | grep -i "opentelemetry"

# Verify agent file exists
docker exec batch-processing-app ls -la /app/opentelemetry-javaagent.jar
```

#### 2. No Traces in Tempo

```bash
# Check OTel Collector logs
docker logs batch-otel-collector

# Verify endpoints
curl http://localhost:4318/v1/traces -X POST -H "Content-Type: application/json" -d '{"traces":[]}'
```

#### 3. Missing Metrics

```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Verify OTel Collector metrics endpoint
curl http://localhost:8889/metrics
```

#### 4. Grafana Data Source Issues

```bash
# Check data source connectivity in Grafana
# Go to Configuration > Data Sources > Test
```

### Debug Commands

```bash
# Check all container logs
./observability.sh logs

# Check specific service
docker logs batch-tempo

# Check network connectivity
docker exec batch-processing-app ping otel-collector

# Check OTel configuration
docker exec batch-otel-collector cat /etc/otelcol-contrib/otel-collector-config.yaml
```

## 📁 File Structure

```
.
├── docker-compose.yml                    # Main application compose
├── observation-compose.yaml              # LGTM stack compose
├── docker-compose.observability.yml      # Combined override (experimental)
├── observability.sh                      # Management script
├── Dockerfile                           # Updated with OTel agent
└── docker/
    ├── otel/
    │   └── otel-collector-config.yaml   # Collector configuration
    ├── tempo/
    │   └── tempo.yaml                   # Tempo configuration
    ├── prometheus/
    │   └── prometheus.yml               # Prometheus targets
    ├── mimir/
    │   └── mimir.yaml                   # Mimir configuration
    └── grafana/
        └── datasources/
            └── datasources.yaml         # Pre-configured data sources
```

## 🎯 Benefits

### Development

- **Real-time debugging** with trace correlation
- **Performance analysis** across all components
- **Error tracking** with full context
- **Local development observability** matching production

### Production

- **Comprehensive monitoring** of batch operations
- **Long-term metrics storage** with Mimir
- **Centralized logging** with structured search
- **Distributed tracing** for complex workflows
- **Alert integration** ready for Prometheus AlertManager

## 🔮 Next Steps

1. **Custom Dashboards**: Create specific dashboards for batch job monitoring
2. **Alerting Rules**: Set up Prometheus alerts for job failures
3. **Log Parsing**: Configure Loki to parse structured application logs
4. **Trace Sampling**: Configure sampling for high-volume environments
5. **Production Deployment**: Adapt configuration for Kubernetes/production

---

**Integration Status: COMPLETE** ✅  
**LGTM Stack**: Grafana + Loki + Tempo + Mimir + Prometheus  
**Auto-instrumentation**: OpenTelemetry Java Agent  
**Management**: `./observability.sh` script
