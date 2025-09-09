#!/bin/bash

# Script สำหรับ deploy บน Kubernetes cluster

echo "🚀 Deploying Batch Processing Application to Kubernetes..."

# สร้าง namespace
echo "📁 Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# สร้าง secrets และ configmaps
echo "🔐 Creating secrets and configmaps..."
kubectl apply -f k8s/mysql-secret.yaml
kubectl apply -f k8s/mysql-configmap.yaml
kubectl apply -f k8s/csv-data-configmap.yaml

# สร้าง persistent volume claim
echo "💾 Creating persistent volume claim..."
kubectl apply -f k8s/mysql-pvc.yaml

# Deploy MySQL
echo "🗄️ Deploying MySQL..."
kubectl apply -f k8s/mysql-deployment.yaml

# รอให้ MySQL พร้อม
echo "⏳ Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n batch-processing --timeout=300s

if [ $? -eq 0 ]; then
    echo "✅ MySQL is ready!"
    
    # รัน Batch Job
    echo "🔄 Running Batch Job..."
    kubectl apply -f k8s/batch-job.yaml
    
    echo "📊 Checking job status..."
    kubectl get jobs -n batch-processing
    
    echo "📝 Job logs:"
    sleep 10
    kubectl logs -l app=batch-processing -n batch-processing --tail=50
    
    echo "🎉 Deployment completed!"
else
    echo "❌ MySQL failed to start. Aborting deployment."
    exit 1
fi
