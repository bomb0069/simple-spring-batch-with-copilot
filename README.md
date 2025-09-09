# Batch Processing Application

ระบบ Batch Processing ที่พัฒนาด้วย Java Spring Batch สำหรับการประมวลผลไฟล์ CSV และคำนวณ VAT

## คุณสมบัติ

- **Step 1**: อ่านข้อมูลจากไฟล์ CSV ที่มี 2 columns: Price และ VAT Rate
- **Step 2**: คำนวณ VAT Amount และ Total Price
- **Step 3**: บันทึกข้อมูลลงฐานข้อมูล MySQL

## โครงสร้างโปรเจค

```
batch/
├── src/
│   ├── main/
│   │   ├── java/com/example/batch/
│   │   │   ├── BatchProcessingApplication.java
│   │   │   ├── config/
│   │   │   │   └── BatchConfiguration.java
│   │   │   ├── model/
│   │   │   │   ├── PriceInput.java
│   │   │   │   └── PriceCalculation.java
│   │   │   ├── processor/
│   │   │   │   └── VatCalculationProcessor.java
│   │   │   └── repository/
│   │   │       └── PriceCalculationRepository.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── input-data.csv
│   └── test/
├── docker/
│   └── mysql-init/
│       └── init.sql
├── k8s/
│   ├── namespace.yaml
│   ├── mysql-secret.yaml
│   ├── mysql-configmap.yaml
│   ├── mysql-pvc.yaml
│   ├── mysql-deployment.yaml
│   ├── csv-data-configmap.yaml
│   └── batch-job.yaml
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## การติดตั้งและรัน

### 1. รันด้วย Docker Compose (localhost)

```bash
# สร้างและรัน services
docker-compose up --build

# หยุด services
docker-compose down
```

### 2. รันด้วย Maven (ต้องมี MySQL รันอยู่)

```bash
# คอมไพล์โปรเจค
./mvnw clean package

# รันแอปพลิเคชัน
./mvnw spring-boot:run
```

### 3. Deployment บน Kubernetes

```bash
# สร้าง namespace
kubectl apply -f k8s/namespace.yaml

# สร้าง secrets และ configmaps
kubectl apply -f k8s/mysql-secret.yaml
kubectl apply -f k8s/mysql-configmap.yaml
kubectl apply -f k8s/csv-data-configmap.yaml

# สร้าง persistent volume claim
kubectl apply -f k8s/mysql-pvc.yaml

# Deploy MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# รอให้ MySQL พร้อม
kubectl wait --for=condition=ready pod -l app=mysql -n batch-processing --timeout=300s

# รัน Batch Job
kubectl apply -f k8s/batch-job.yaml

# ตรวจสอบสถานะ Job
kubectl get jobs -n batch-processing
kubectl logs -l app=batch-processing -n batch-processing
```

## การตั้งค่าฐานข้อมูล

### ข้อมูลการเชื่อมต่อ MySQL

- **Host**: localhost (Docker Compose) หรือ mysql-service (Kubernetes)
- **Port**: 3306
- **Database**: batch_db
- **Username**: batch_user
- **Password**: batch_password

### โครงสร้างตาราง

```sql
CREATE TABLE price_calculations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_price DECIMAL(10,2) NOT NULL,
    vat_rate DECIMAL(5,4) NOT NULL,
    vat_amount DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## ข้อมูลตัวอย่าง

ไฟล์ `input-data.csv` มีข้อมูลตัวอย่าง:

```csv
price,vatRate
100.00,0.07
250.50,0.07
75.25,0.10
500.00,0.07
```

## การกำหนดค่า

### Environment Variables

- `SPRING_DATASOURCE_URL`: URL สำหรับเชื่อมต่อฐานข้อมูล
- `SPRING_DATASOURCE_USERNAME`: ชื่อผู้ใช้ฐานข้อมูล
- `SPRING_DATASOURCE_PASSWORD`: รหัสผ่านฐานข้อมูล

### Application Properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/batch_db
spring.datasource.username=batch_user
spring.datasource.password=batch_password
spring.jpa.hibernate.ddl-auto=update
spring.batch.job.enabled=true
```

## การตรวจสอบผลลัพธ์

หลังจากรัน Batch Job สำเร็จ สามารถตรวจสอบข้อมูลในฐานข้อมูลได้:

```sql
SELECT * FROM price_calculations ORDER BY created_at DESC;
```

## การทดสอบ

```bash
# รันการทดสอบ
./mvnw test

# รันการทดสอบเฉพาะ Batch
./mvnw test -Dtest=BatchConfigurationTest
```

## การปรับแต่ง

### เปลี่ยนขนาด Chunk

แก้ไขใน `BatchConfiguration.java`:

```java
.chunk(10, transactionManager) // เปลี่ยนจาก 10 เป็นจำนวนที่ต้องการ
```

### เพิ่มการตรวจสอบข้อมูล

แก้ไขใน `VatCalculationProcessor.java` เพื่อเพิ่มการตรวจสอบข้อมูลก่อนประมวลผล

## การแก้ไขปัญหา

### ปัญหาการเชื่อมต่อฐานข้อมูล

1. ตรวจสอบว่า MySQL กำลังรันอยู่
2. ตรวจสอบ username/password
3. ตรวจสอบ network connectivity

### ปัญหา Kubernetes

```bash
# ตรวจสอบ pods
kubectl get pods -n batch-processing

# ดู logs
kubectl logs <pod-name> -n batch-processing

# ดูรายละเอียด pod
kubectl describe pod <pod-name> -n batch-processing
```

## เทคโนโลยีที่ใช้

- Java 17
- Spring Boot 3.3.2
- Spring Batch
- Spring Data JPA
- MySQL 8.0
- Docker & Docker Compose
- Kubernetes
- Maven
