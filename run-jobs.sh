#!/bin/bash

# Batch Jobs Runner using docker-compose.jobs.yml
# Usage: ./run-jobs.sh [job-profile]

set -e

echo "🚀 Spring Batch Jobs Runner (Docker Compose)"
echo "=============================================="

# Function to show usage
show_usage() {
    echo "📖 Available Job Profiles:"
    echo "   • vat-calculation     - Run VAT calculation job only"
    echo "   • export-json         - Run JSON export job only"
    echo "   • all-jobs           - Run both jobs sequentially"
    echo ""
    echo "💡 Usage Examples:"
    echo "   ./run-jobs.sh vat-calculation"
    echo "   ./run-jobs.sh export-json"
    echo "   ./run-jobs.sh all-jobs"
    echo ""
    echo "🔧 Infrastructure Commands:"
    echo "   ./run-jobs.sh start      - Start MySQL and Adminer only"
    echo "   ./run-jobs.sh stop       - Stop all services"
    echo "   ./run-jobs.sh logs       - Show logs from all services"
    echo "   ./run-jobs.sh clean      - Clean up everything"
    echo ""
    echo "🌐 Access URLs:"
    echo "   • MySQL: localhost:3307"
    echo "   • Adminer: http://localhost:8081"
}

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose not found. Please install Docker Compose."
    exit 1
fi

# Get the job profile
JOB_PROFILE=${1:-""}

case "$JOB_PROFILE" in
    "vat-calculation")
        echo "🎯 Running VAT Calculation Job"
        echo "📋 Starting infrastructure..."
        docker-compose -f docker-compose.jobs.yml up -d mysql adminer
        echo "⏳ Waiting for MySQL to be ready..."
        sleep 10
        echo "🚀 Running VAT calculation job..."
        docker-compose -f docker-compose.jobs.yml --profile vat-calculation up --build vat-calculation-job
        echo "✅ VAT Calculation job completed!"
        ;;
        
    "export-json")
        echo "🎯 Running Export JSON Job"
        echo "📋 Starting infrastructure..."
        docker-compose -f docker-compose.jobs.yml up -d mysql adminer
        echo "⏳ Waiting for MySQL to be ready..."
        sleep 10
        echo "🚀 Running export JSON job..."
        docker-compose -f docker-compose.jobs.yml --profile export-json up --build export-json-job
        echo "✅ Export JSON job completed!"
        ;;
        
    "all-jobs")
        echo "🎯 Running All Jobs Sequentially"
        echo "📋 Starting infrastructure..."
        docker-compose -f docker-compose.jobs.yml up -d mysql adminer
        echo "⏳ Waiting for MySQL to be ready..."
        sleep 10
        echo "🚀 Running all jobs..."
        docker-compose -f docker-compose.jobs.yml --profile all-jobs up --build all-jobs
        echo "✅ All jobs completed!"
        ;;
        
    "start")
        echo "🔧 Starting Infrastructure Services"
        docker-compose -f docker-compose.jobs.yml up -d mysql adminer
        echo "✅ Infrastructure started!"
        echo "🌐 Adminer: http://localhost:8081"
        echo "🗄️  MySQL: localhost:3307"
        ;;
        
    "stop")
        echo "🛑 Stopping All Services"
        docker-compose -f docker-compose.jobs.yml down
        echo "✅ All services stopped!"
        ;;
        
    "logs")
        echo "📋 Showing Logs"
        docker-compose -f docker-compose.jobs.yml logs -f
        ;;
        
    "clean")
        echo "🧹 Cleaning Up Everything"
        docker-compose -f docker-compose.jobs.yml down -v --remove-orphans
        docker system prune -f
        echo "✅ Cleanup completed!"
        ;;
        
    "")
        show_usage
        ;;
        
    *)
        echo "❌ Unknown job profile: $JOB_PROFILE"
        echo ""
        show_usage
        exit 1
        ;;
esac
