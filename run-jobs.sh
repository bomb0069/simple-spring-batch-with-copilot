#!/bin/bash

# Batch Jobs Runner - Supports Docker Compose and direct execution
# Usage: ./run-jobs.sh [job-profile] [--direct]

set -e

echo "🚀 Spring Batch Jobs Runner"
echo "============================"

# Function to show usage
show_usage() {
    echo "📖 Available Job Profiles:"
    echo "   • vat-calculation     - Run VAT calculation job only"
    echo "   • export-json         - Run JSON export job only"
    echo "   • all-jobs           - Run both jobs sequentially"
    echo ""
    echo "💡 Docker Compose Usage (default):"
    echo "   ./run-jobs.sh vat-calculation"
    echo "   ./run-jobs.sh export-json"
    echo "   ./run-jobs.sh all-jobs"
    echo ""
    echo "🎯 Direct Execution Usage:"
    echo "   ./run-jobs.sh vat-calculation --direct"
    echo "   ./run-jobs.sh export-json --direct"
    echo "   ./run-jobs.sh all-jobs --direct"
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

# Function to run jobs directly (without Docker)
run_direct_jobs() {
    local job_type=$1
    
    echo "🎯 Direct Execution Mode"
    
    # Check if JAR file exists
    if [ ! -f "target/batch-processing-1.0.0.jar" ]; then
        echo "📦 Building JAR file..."
        ./mvnw clean package -DskipTests
    fi
    
    # Wait for MySQL to be ready if running
    if docker ps | grep -q batch-mysql-jobs; then
        echo "⏳ MySQL detected, waiting for it to be ready..."
        sleep 5
    fi
    
    case "$job_type" in
        "vat-calculation")
            echo "🚀 Running VAT Calculation Job..."
            java -Dspring.main.web-application-type=none \
                 -Dspring.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                 -Dbusiness.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                 -jar target/batch-processing-1.0.0.jar --job=vat-calculation
            if [ $? -eq 0 ]; then
                echo "✅ VAT Calculation completed!"
            else
                echo "❌ VAT Calculation job failed!"
                exit 1
            fi
            ;;
            
        "export-json")
            echo "🚀 Running Export JSON Job..."
            java -Dspring.main.web-application-type=none \
                 -Dspring.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                 -Dbusiness.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                 -jar target/batch-processing-1.0.0.jar --job=export-json
            if [ $? -eq 0 ]; then
                echo "✅ Export JSON completed!"
            else
                echo "❌ Export JSON job failed!"
                exit 1
            fi
            ;;
            
        "all-jobs")
            echo "🚀 Running VAT Calculation Job..."
            java -Dspring.main.web-application-type=none \
                 -Dspring.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                 -Dbusiness.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                 -jar target/batch-processing-1.0.0.jar --job=vat-calculation
            VAT_EXIT_CODE=$?
            
            if [ $VAT_EXIT_CODE -eq 0 ]; then
                echo "✅ VAT Calculation completed!"
                echo "🚀 Running Export JSON Job..."
                java -Dspring.main.web-application-type=none \
                     -Dspring.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                     -Dbusiness.datasource.url="jdbc:mysql://localhost:3307/batch_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                     -jar target/batch-processing-1.0.0.jar --job=export-json
                EXPORT_EXIT_CODE=$?
                
                if [ $EXPORT_EXIT_CODE -eq 0 ]; then
                    echo "✅ All jobs completed successfully!"
                    exit 0
                else
                    echo "❌ Export JSON job failed with exit code: $EXPORT_EXIT_CODE"
                    exit $EXPORT_EXIT_CODE
                fi
            else
                echo "❌ VAT Calculation job failed with exit code: $VAT_EXIT_CODE"
                exit $VAT_EXIT_CODE
            fi
            ;;
    esac
}

# Get the job profile and execution mode
JOB_PROFILE=${1:-""}
EXECUTION_MODE=${2:-""}

# Check for direct execution mode
if [ "$EXECUTION_MODE" = "--direct" ] || [ "$JOB_PROFILE" = "--direct" ]; then
    if [ "$JOB_PROFILE" = "--direct" ]; then
        echo "❌ Job profile required when using --direct mode"
        show_usage
        exit 1
    fi
    run_direct_jobs "$JOB_PROFILE"
    exit 0
fi

# Check if docker-compose is available (only for Docker mode)
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose not found. Please install Docker Compose."
    echo "💡 Alternatively, use --direct mode for local execution"
    exit 1
fi

echo "(Docker Compose Mode)"
echo ""

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
