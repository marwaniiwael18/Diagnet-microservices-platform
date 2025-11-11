#!/bin/bash

echo "🚀 Starting DiagNet Infrastructure..."
echo ""

# Start only database and MQTT broker (no building Java services)
docker-compose up -d timescaledb mosquitto pgadmin

echo ""
echo "Waiting for services to be healthy..."
sleep 10

echo ""
echo "✅ Infrastructure Started!"
echo ""
echo "Services Running:"
echo "  • TimescaleDB:  localhost:5432"
echo "  • Mosquitto:    localhost:1883"
echo "  • pgAdmin:      http://localhost:5050"
echo ""
echo "Now you can run services locally:"
echo "  cd backend/microservices/collector-service"
echo "  JAVA_HOME=/Users/macbook/Library/Java/JavaVirtualMachines/ms-21.0.6/Contents/Home java -jar target/collector-service-1.0.0.jar"
echo ""
