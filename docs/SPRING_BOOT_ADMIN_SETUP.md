# Spring Boot Admin Server Setup

# สำหรับ Monitor Spring Batch Jobs

## 1. เพิ่ม dependencies ใน pom.xml ของ Batch Application

```xml
<!-- Spring Boot Admin Client -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>3.1.5</version>
</dependency>

<!-- Spring Boot Actuator (required for monitoring) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 2. สร้าง Spring Boot Admin Server แยก

### pom.xml สำหรับ Admin Server

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
    <version>3.1.5</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### AdminServerApplication.java

```java
@SpringBootApplication
@EnableAdminServer
public class AdminServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}
```

## 3. Configuration

### application.properties ของ Batch App

```properties
# Spring Boot Admin Client
spring.boot.admin.client.url=http://admin-server:8081
spring.boot.admin.client.instance.name=Batch Processing App

# Actuator endpoints
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.info.env.enabled=true

# Application info
info.app.name=VAT Calculation Batch
info.app.description=CSV Processing with VAT Calculation
info.app.version=1.0.0
```

### application.properties ของ Admin Server

```properties
server.port=8081
spring.application.name=batch-admin-server
```

## 4. Docker Compose Integration

```yaml
services:
  admin-server:
    build: ./admin-server
    container_name: batch-admin-server
    ports:
      - "8081:8081"
    networks:
      - batch-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  batch-app:
    # existing config...
    environment:
      - SPRING_BOOT_ADMIN_CLIENT_URL=http://admin-server:8081
      # other env vars...
```

## 5. Features ที่ได้

### 📊 Application Overview

- Application status (UP/DOWN)
- Memory usage, CPU usage
- JVM metrics
- Custom metrics

### 🔍 Spring Batch Monitoring

- Job execution history
- Step execution details
- Job parameters
- Execution times
- Success/failure rates

### 📱 Real-time Monitoring

- Live metrics dashboard
- Log streaming
- Health checks
- Performance graphs

### 🚨 Alerting

- Email notifications
- Slack integration
- Custom webhook alerts
- Threshold-based alerts
