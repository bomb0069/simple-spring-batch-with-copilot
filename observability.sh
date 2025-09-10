#!/bin/bash

# OpenTelemetry LGTM Stack Management Script
# Usage: ./observability.sh [start|stop|restart|logs|clean]

set -e

COMPOSE_FILES="-f docker-compose.yml -f observation-compose.yaml"
PROJECT_NAME="batch-observability"

case "${1:-start}" in
    "start")
        echo "🚀 Starting LGTM Observability Stack..."
        
        # Create observability network if it doesn't exist
        docker network create observability 2>/dev/null || echo "Network 'observability' already exists"
        
        # Start observability stack first
        echo "📊 Starting observability services..."
        docker-compose -f observation-compose.yaml -p ${PROJECT_NAME} up -d
        
        # Wait for services to be ready
        echo "⏳ Waiting for services to be ready..."
        sleep 10
        
        # Start main application
        echo "🔧 Starting batch application..."
        docker-compose up -d
        
        echo ""
        echo "✅ LGTM Stack is starting up!"
        echo ""
        echo "📊 Access URLs:"
        echo "  - Grafana:     http://localhost:3000 (admin/admin)"
        echo "  - Prometheus:  http://localhost:9090"
        echo "  - Tempo:       http://localhost:3200"
        echo "  - Loki:        http://localhost:3100"
        echo "  - Mimir:       http://localhost:9009"
        echo "  - Batch App:   http://localhost:8090"
        echo "  - Adminer:     http://localhost:8080"
        echo ""
        echo "🔍 Check logs with: ./observability.sh logs"
        ;;
        
    "stop")
        echo "🛑 Stopping LGTM Observability Stack..."
        docker-compose down
        docker-compose -f observation-compose.yaml -p ${PROJECT_NAME} down
        echo "✅ Stack stopped"
        ;;
        
    "restart")
        echo "🔄 Restarting LGTM Observability Stack..."
        $0 stop
        sleep 5
        $0 start
        ;;
        
    "logs")
        SERVICE=${2:-""}
        if [ -n "$SERVICE" ]; then
            echo "📋 Showing logs for $SERVICE..."
            docker-compose logs -f "$SERVICE"
        else
            echo "📋 Showing logs for all services..."
            echo "Main application logs:"
            docker-compose logs --tail=50 batch-app
            echo ""
            echo "Observability stack logs:"
            docker-compose -f observation-compose.yaml -p ${PROJECT_NAME} logs --tail=20
        fi
        ;;
        
    "clean")
        echo "🧹 Cleaning up LGTM Observability Stack..."
        docker-compose down -v --remove-orphans
        docker-compose -f observation-compose.yaml -p ${PROJECT_NAME} down -v --remove-orphans
        docker network rm observability 2>/dev/null || echo "Network 'observability' already removed"
        docker system prune -f --volumes
        echo "✅ Cleanup complete"
        ;;
        
    "status")
        echo "📊 LGTM Stack Status:"
        echo ""
        echo "Main Application:"
        docker-compose ps
        echo ""
        echo "Observability Stack:"
        docker-compose -f observation-compose.yaml -p ${PROJECT_NAME} ps
        ;;
        
    "health")
        echo "🏥 Health Check:"
        echo ""
        
        # Check if services are responding
        services=(
            "Grafana:http://localhost:3000/api/health"
            "Prometheus:http://localhost:9090/-/healthy"
            "Tempo:http://localhost:3200/ready"
            "Loki:http://localhost:3100/ready"
            "Batch App:http://localhost:8090/actuator/health"
        )
        
        for service in "${services[@]}"; do
            name=$(echo $service | cut -d: -f1)
            url=$(echo $service | cut -d: -f2-)
            
            if curl -s -f "$url" > /dev/null 2>&1; then
                echo "✅ $name is healthy"
            else
                echo "❌ $name is not responding"
            fi
        done
        ;;
        
    *)
        echo "Usage: $0 {start|stop|restart|logs [service]|clean|status|health}"
        echo ""
        echo "Commands:"
        echo "  start   - Start the full LGTM observability stack"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  logs    - Show logs (optionally for specific service)"
        echo "  clean   - Remove all containers, volumes, and networks"
        echo "  status  - Show status of all services"
        echo "  health  - Check if services are responding"
        echo ""
        echo "Examples:"
        echo "  ./observability.sh start"
        echo "  ./observability.sh logs batch-app"
        echo "  ./observability.sh health"
        exit 1
        ;;
esac
