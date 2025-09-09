# Makefile สำหรับ Batch Processing Application

.PHONY: help build test run-local docker-build docker-up docker-down k8s-deploy k8s-clean

help: ## แสดงความช่วยเหลือ
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build โปรเจคด้วย Maven
	./mvnw clean package

test: ## รันการทดสอบ
	./mvnw test

run-local: ## รันแอปพลิเคชันแบบ local (ต้องมี MySQL)
	./mvnw spring-boot:run

docker-build: ## Build Docker image
	docker build -t batch-processing:latest .

docker-up: ## รัน services ด้วย Docker Compose
	docker-compose up --build -d

docker-down: ## หยุด Docker Compose services
	docker-compose down

docker-logs: ## ดู logs ของ Docker Compose
	docker-compose logs -f

k8s-deploy: ## Deploy บน Kubernetes
	./deploy.sh

k8s-clean: ## ลบ resources จาก Kubernetes
	kubectl delete namespace batch-processing --ignore-not-found=true

k8s-logs: ## ดู logs ของ Kubernetes job
	kubectl logs -l app=batch-processing -n batch-processing --tail=100

k8s-status: ## ตรวจสอบสถานะ Kubernetes
	@echo "Namespace:"
	@kubectl get namespace batch-processing 2>/dev/null || echo "Namespace not found"
	@echo "Pods:"
	@kubectl get pods -n batch-processing 2>/dev/null || echo "No pods found"
	@echo "Jobs:"
	@kubectl get jobs -n batch-processing 2>/dev/null || echo "No jobs found"

clean: ## ลบไฟล์ที่ build แล้ว
	./mvnw clean
	docker system prune -f
