#!/bin/bash

# Test script for Order Management System API
# Make sure the server is running on port 8089 before executing this script

BASE_URL="http://localhost:8089"

echo "=== Order Management System API Test ==="
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Login
echo "1. Testing Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}')

echo "Login Response: $LOGIN_RESPONSE"
echo ""

# Test 2: Get all orders
echo "2. Testing Get All Orders..."
ORDERS_RESPONSE=$(curl -s -X GET "$BASE_URL/orders")
echo "Orders Response: $ORDERS_RESPONSE"
echo ""

# Test 3: Create a new order
echo "3. Testing Create Order..."
CREATE_ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "customerName": "Test Customer",
    "shippingAddress": "123 Test St, Test City, TS",
    "billingAddress": "123 Test St, Test City, TS",
    "notes": "Test order",
    "items": [
      {
        "productId": "TEST-001",
        "productName": "Test Product",
        "quantity": 2,
        "price": 99.99,
        "description": "A test product"
      }
    ]
  }')

echo "Create Order Response: $CREATE_ORDER_RESPONSE"
echo ""

# Test 4: Get all shipments
echo "4. Testing Get All Shipments..."
SHIPMENTS_RESPONSE=$(curl -s -X GET "$BASE_URL/ext/shipment")
echo "Shipments Response: $SHIPMENTS_RESPONSE"
echo ""

# Test 5: Create a shipment
echo "5. Testing Create Shipment..."
CREATE_SHIPMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/ext/shipment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "carrier": "Test Carrier",
    "shippingAddress": "123 Test St, Test City, TS"
  }')

echo "Create Shipment Response: $CREATE_SHIPMENT_RESPONSE"
echo ""

# Test 6: List files
echo "6. Testing List Files..."
FILES_RESPONSE=$(curl -s -X GET "$BASE_URL/files")
echo "Files Response: $FILES_RESPONSE"
echo ""

echo "=== API Test Complete ==="
echo ""
echo "Note: File upload test requires a file to be uploaded via multipart/form-data"
echo "You can test file upload using tools like Postman or curl with a file" 