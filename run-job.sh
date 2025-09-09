#!/bin/bash

# Batch Job Command Line Runner
# Usage: ./run-job.sh [job-name]

echo "🚀 Spring Batch Command Line Job Runner"
echo "========================================"

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose not found. Please install Docker Compose."
    exit 1
fi

# Start MySQL and Adminer if not running
if ! docker ps | grep -q batch-mysql; then
    echo "🔧 Starting MySQL and Adminer services..."
    docker-compose up -d mysql adminer
    echo "⏳ Waiting for MySQL to be ready..."
    sleep 10
fi

# Job parameter
JOB_NAME=${1:-""}

if [ -z "$JOB_NAME" ]; then
    echo "📖 Available Jobs:"
    echo "   • vat-calculation     - Process CSV and calculate VAT"
    echo "   • export-json         - Export calculations to JSON"
    echo "   • vatCalculationJob   - Same as vat-calculation"
    echo "   • exportVatCalculationsJob - Same as export-json"
    echo ""
    echo "💡 Usage Examples:"
    echo "   ./run-job.sh vat-calculation"
    echo "   ./run-job.sh export-json"
    echo ""
    echo "🌐 Alternative: Use REST API endpoints"
    echo "   curl -X POST http://localhost:8090/api/batch/run/vat-calculation"
    echo "   curl -X POST http://localhost:8090/api/batch/run/export-json"
    exit 0
fi

echo "🎯 Running job: $JOB_NAME"
echo "📋 Command: docker run --rm --network batch_batch-network batch-batch-app --job=$JOB_NAME"
echo ""

# Run the job
docker run --rm --network batch_batch-network batch-batch-app --job=$JOB_NAME

echo ""
echo "✅ Job execution completed!"
