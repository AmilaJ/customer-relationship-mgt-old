#!/bin/bash

# CRM System Startup Script
# This script helps you start both the Java backend and Angular frontend

echo "🚀 Starting CRM System..."
echo ""

# Check if Java backend is running
echo "📋 Checking Java backend status..."
if curl -s http://localhost:8089/orders > /dev/null 2>&1; then
    echo "✅ Java backend is already running on port 8089"
else
    echo "❌ Java backend is not running"
    echo ""
    echo "To start the Java backend:"
    echo "1. Navigate to the legacy_crm directory"
    echo "2. Run: mvn clean package"
    echo "3. Run: java -jar target/order-management-1.0.0.jar"
    echo ""
    echo "Or use the test script: ./test_api.sh"
    echo ""
fi

echo ""
echo "🌐 Starting Angular frontend..."
echo "Frontend will be available at: http://localhost:4200"
echo ""
echo "Demo credentials:"
echo "- Username: admin, Password: admin123"
echo "- Username: user1, Password: user123"
echo "- Username: user2, Password: user123"
echo "- Username: demo, Password: demo123"
echo ""

# Start Angular development server
npm start 